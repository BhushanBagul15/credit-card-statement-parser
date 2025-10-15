package com.example.demo.parser.issuer;



import com.example.demo.parser.extractor.AdvancedPDFExtractor;
import com.example.demo.parser.extractor.PDFTextExtractor;
import com.example.demo.parser.model.StatementData;
import com.example.demo.parser.model.Transaction;
import com.example.demo.parser.CreditCardParser;
import com.example.demo.parser.util.AmountParser;
import com.example.demo.parser.util.DateParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Improved HDFC Parser that handles real PDF formats
 * Uses advanced extraction techniques for complex layouts
 */
public class ImprovedHDFCParser implements CreditCardParser {

    private static final Logger logger = LoggerFactory.getLogger(ImprovedHDFCParser.class);
    private static final String ISSUER_NAME = "HDFC Bank";

    @Override
    public StatementData parse(File pdfFile) throws IOException {
        logger.info("Parsing HDFC Bank statement with advanced extractor: {}", pdfFile.getName());

        // Extract text with multiple strategies
        String rawText = PDFTextExtractor.extractText(pdfFile);
        String layoutText = AdvancedPDFExtractor.extractTextWithLayout(pdfFile);
        Map<String, String> regions = AdvancedPDFExtractor.extractByRegions(pdfFile);
        List<List<String>> tables = AdvancedPDFExtractor.extractTables(pdfFile);

        StatementData data = StatementData.builder()
                .issuerName(ISSUER_NAME)
                .build();

        // Extract using multiple fallback strategies
        extractCardLastFourDigits(rawText, layoutText, data);
        extractCardVariant(rawText, layoutText, data);
        extractStatementDate(rawText, layoutText, data);
        extractPaymentDueDate(rawText, layoutText, data);
        extractTotalAmountDue(rawText, layoutText, regions, data);

        // Additional fields
        extractCreditLimit(rawText, layoutText, data);
        extractAvailableCredit(rawText, layoutText, data);
        extractTransactions(tables, rawText, data);

        logger.info("HDFC Parsing complete. Valid: {}", data.isValid());
        return data;
    }

    @Override
    public boolean supports(String text) {
        if (text == null) return false;
        String upper = text.toUpperCase();
        return upper.contains("HDFC BANK") ||
                upper.contains("HDFCBANK") ||
                (upper.contains("HDFC") && (upper.contains("CREDIT CARD") || upper.contains("STATEMENT")));
    }

    @Override
    public String getIssuerName() {
        return ISSUER_NAME;
    }

    /**
     * Extract card number with multiple pattern attempts
     */
    private void extractCardLastFourDigits(String rawText, String layoutText, StatementData data) {
        String[] patterns = {
                "(?i)Card\\s+Number\\s*:?\\s*(?:X+\\s*){3}(\\d{4})",
                "(?i)Card\\s+No\\.?\\s*:?\\s*(?:[X*]\\s*){12}(\\d{4})",
                "(?i)(?:ending|ends)\\s+(?:in|with)\\s*:?\\s*(\\d{4})",
                "(?i)\\*{12}(\\d{4})",
                "XXXX\\s+XXXX\\s+XXXX\\s+(\\d{4})"
        };

        for (String pattern : patterns) {
            String result = extractWithPattern(rawText, pattern);
            if (result == null) {
                result = extractWithPattern(layoutText, pattern);
            }

            if (result != null && result.matches("\\d{4}")) {
                data.setCardLastFourDigits(result);
                logger.debug("Extracted card number: ****{}", result);
                return;
            }
        }

        logger.warn("Could not extract card number");
    }

    /**
     * Extract card variant with fuzzy matching
     */
    private void extractCardVariant(String rawText, String layoutText, StatementData data) {
        // Try with keyword search
        String result = AdvancedPDFExtractor.findValueAfterKeyword(rawText,
                "Card Type", "Product", "Card Variant", "Card Name", "Card Product");

        if (result == null) {
            result = AdvancedPDFExtractor.findValueAfterKeyword(layoutText,
                    "Card Type", "Product", "Card Variant");
        }

        // Known HDFC card variants
        String[] knownVariants = {
                "MoneyBack", "MoneyBack+", "Regalia", "Regalia First", "Regalia Gold",
                "Diners Club", "Diners Black", "Infinia", "Millennia", "Freedom",
                "Platinum", "Titanium", "Visa Signature", "World MasterCard"
        };

        // Try to match known variants in the text
        if (result == null) {
            for (String variant : knownVariants) {
                if (rawText.toUpperCase().contains(variant.toUpperCase())) {
                    result = variant;
                    break;
                }
            }
        }

        if (result != null) {
            // Clean up the result
            result = result.replaceAll("(?i)(card|credit|hdfc)", "").trim();
            data.setCardVariant(result);
            logger.debug("Extracted card variant: {}", result);
        }
    }

    /**
     * Extract statement date with multiple formats
     */
    private void extractStatementDate(String rawText, String layoutText, StatementData data) {
        // Try direct keyword extraction
        String dateStr = AdvancedPDFExtractor.findValueAfterKeyword(rawText,
                "Statement Date", "Date of Statement", "Statement Period", "Bill Date");

        if (dateStr == null) {
            dateStr = AdvancedPDFExtractor.findValueAfterKeyword(layoutText,
                    "Statement Date", "Date of Statement");
        }

        // Extract all dates and pick the most likely one
        if (dateStr == null) {
            List<String> allDates = AdvancedPDFExtractor.extractAllDates(rawText);
            if (!allDates.isEmpty()) {
                dateStr = allDates.get(0); // Usually first date is statement date
            }
        }

        if (dateStr != null) {
            LocalDate date = DateParser.parseDate(dateStr);
            if (date != null && DateParser.isValidDate(date)) {
                data.setStatementDate(date);
                logger.debug("Extracted statement date: {}", date);
            }
        }
    }

    /**
     * Extract payment due date
     */
    private void extractPaymentDueDate(String rawText, String layoutText, StatementData data) {
        String dateStr = AdvancedPDFExtractor.findValueAfterKeyword(rawText,
                "Payment Due Date", "Due Date", "Pay By", "Payment Due By",
                "Last Date of Payment", "Payment Deadline");

        if (dateStr == null) {
            dateStr = AdvancedPDFExtractor.findValueAfterKeyword(layoutText,
                    "Payment Due Date", "Due Date", "Pay By");
        }

        if (dateStr != null) {
            LocalDate date = DateParser.parseDate(dateStr);
            if (date != null && DateParser.isValidDate(date)) {
                data.setPaymentDueDate(date);
                logger.debug("Extracted due date: {}", date);
            }
        }
    }

    /**
     * Extract total amount due with multiple strategies
     */
    private void extractTotalAmountDue(String rawText, String layoutText,
                                       Map<String, String> regions, StatementData data) {
        String amountStr = AdvancedPDFExtractor.findValueAfterKeyword(rawText,
                "Total Amount Due", "Amount Due", "Outstanding Balance",
                "Total Outstanding", "Payment Amount", "Amount Payable");

        if (amountStr == null && regions.containsKey("account")) {
            amountStr = AdvancedPDFExtractor.findValueAfterKeyword(regions.get("account"),
                    "Total Amount Due", "Amount Due");
        }

        // Fallback: extract all amounts and pick the largest (likely the total due)
        if (amountStr == null) {
            List<String> allAmounts = AdvancedPDFExtractor.extractAllAmounts(rawText);
            if (!allAmounts.isEmpty()) {
                // Find the largest amount
                BigDecimal maxAmount = BigDecimal.ZERO;
                for (String amt : allAmounts) {
                    BigDecimal parsed = AmountParser.parseAmount(amt);
                    if (parsed != null && parsed.compareTo(maxAmount) > 0) {
                        maxAmount = parsed;
                        amountStr = amt;
                    }
                }
            }
        }

        if (amountStr != null) {
            BigDecimal amount = AmountParser.parseAmount(amountStr);
            if (amount != null && AmountParser.isValidAmount(amount)) {
                data.setTotalAmountDue(amount);
                logger.debug("Extracted total due: {}", amount);
            }
        }
    }

    /**
     * Extract credit limit
     */
    private void extractCreditLimit(String rawText, String layoutText, StatementData data) {
        String amountStr = AdvancedPDFExtractor.findValueAfterKeyword(rawText,
                "Credit Limit", "Total Limit", "Card Limit");

        if (amountStr == null) {
            amountStr = AdvancedPDFExtractor.findValueAfterKeyword(layoutText,
                    "Credit Limit", "Total Limit");
        }

        if (amountStr != null) {
            BigDecimal amount = AmountParser.parseAmount(amountStr);
            if (amount != null) {
                data.setCreditLimit(amount);
                logger.debug("Extracted credit limit: {}", amount);
            }
        }
    }

    /**
     * Extract available credit
     */
    private void extractAvailableCredit(String rawText, String layoutText, StatementData data) {
        String amountStr = AdvancedPDFExtractor.findValueAfterKeyword(rawText,
                "Available Credit", "Available Limit", "Credit Available");

        if (amountStr == null) {
            amountStr = AdvancedPDFExtractor.findValueAfterKeyword(layoutText,
                    "Available Credit", "Available Limit");
        }

        if (amountStr != null) {
            BigDecimal amount = AmountParser.parseAmount(amountStr);
            if (amount != null) {
                data.setAvailableCredit(amount);
                logger.debug("Extracted available credit: {}", amount);
            }
        }
    }

    /**
     * Extract transactions from table structure
     */
    private void extractTransactions(List<List<String>> tables, String rawText, StatementData data) {
        logger.debug("Extracting transactions from {} table rows", tables.size());

        int transactionCount = 0;

        for (List<String> row : tables) {
            if (row.size() < 3 || transactionCount >= 50) {
                continue;
            }

            // Try to parse as transaction: Date | Description | Amount
            String dateStr = row.get(0);
            LocalDate txnDate = DateParser.parseDate(dateStr);

            if (txnDate != null) {
                String description = row.size() > 1 ? row.get(1) : "";
                String amountStr = row.size() > 2 ? row.get(row.size() - 1) : "";

                BigDecimal amount = AmountParser.parseAmount(amountStr);

                if (amount != null && !description.trim().isEmpty()) {
                    Transaction transaction = Transaction.builder()
                            .transactionDate(txnDate)
                            .description(description.trim())
                            .amount(amount)
                            .type("DEBIT")
                            .build();

                    data.addTransaction(transaction);
                    transactionCount++;
                }
            }
        }

        logger.info("Extracted {} transactions", transactionCount);
    }

    /**
     * Helper method to extract with pattern
     */
    private String extractWithPattern(String text, String pattern) {
        try {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                return m.group(1).trim();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}