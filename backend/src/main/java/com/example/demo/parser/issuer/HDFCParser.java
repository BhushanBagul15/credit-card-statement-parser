package com.example.demo.parser.issuer;

import com.example.demo.parser.extractor.PDFTextExtractor;
import com.example.demo.parser.extractor.PatternExtractor;
import com.example.demo.parser.model.StatementData;
//import com.creditcard.parser.model.Transaction;
import com.example.demo.parser.model.Transaction;
import com.example.demo.parser.CreditCardParser;
import com.example.demo.parser.util.AmountParser;
import com.example.demo.parser.util.DateParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/**
 * Parser for HDFC Bank credit card statements
 */
//@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
public class HDFCParser implements CreditCardParser {

    private static final Logger logger = LoggerFactory.getLogger(HDFCParser.class);
    private static final String ISSUER_NAME = "HDFC Bank";

    // Patterns for HDFC statements
    private static final String CARD_NUMBER_PATTERN = "(?:Card\\s+Number|Card\\s+No\\.?)\\s*:?\\s*(?:X{4}\\s*){3}(\\d{4})";
    private static final String CARD_VARIANT_PATTERN = "(?:Card\\s+Type|Product|Card\\s+Variant)\\s*:?\\s*([A-Za-z\\s+]+?)(?:\\n|$)";
    private static final String STATEMENT_DATE_PATTERN = "(?:Statement\\s+Date|Date\\s+of\\s+Statement)\\s*:?\\s*([\\d]{2}[-/][A-Za-z]{3}[-/][\\d]{4})";
    private static final String DUE_DATE_PATTERN = "(?:Payment\\s+Due\\s+Date|Due\\s+Date|Pay\\s+By)\\s*:?\\s*([\\d]{2}[-/][A-Za-z]{3}[-/][\\d]{4})";
    private static final String TOTAL_DUE_PATTERN = "(?:Total\\s+Amount\\s+Due|Amount\\s+Due|Outstanding\\s+Balance)\\s*:?\\s*(?:Rs\\.?|₹)?\\s*([\\d,]+\\.?\\d*)";
    private static final String CREDIT_LIMIT_PATTERN = "(?:Credit\\s+Limit)\\s*:?\\s*(?:Rs\\.?|₹)?\\s*([\\d,]+\\.?\\d*)";
    private static final String AVAILABLE_CREDIT_PATTERN = "(?:Available\\s+Credit\\s+Limit|Available\\s+Limit)\\s*:?\\s*(?:Rs\\.?|₹)?\\s*([\\d,]+\\.?\\d*)";

    // Transaction pattern: Date Description Amount
    private static final String TRANSACTION_PATTERN = "([\\d]{2}[-/][A-Za-z]{3}[-/][\\d]{2,4})\\s+(.{10,60}?)\\s+(?:Rs\\.?|₹)?\\s*([\\d,]+\\.\\d{2})";

    public HDFCParser() {
        // This public constructor allows instantiation from ParserFactory
    }


    public StatementData parse(File pdfFile) throws IOException {
        logger.info("Parsing HDFC Bank statement: {}", pdfFile.getName());

        // Extract text from PDF
        String text = PDFTextExtractor.extractText(pdfFile);

        // Create statement data object
        StatementData data = StatementData.builder()
                .issuerName(ISSUER_NAME)
                .build();

        // Extract 5 key data points
        extractCardLastFourDigits(text, data);
        extractCardVariant(text, data);
        extractStatementDate(text, data);
        extractPaymentDueDate(text, data);
        extractTotalAmountDue(text, data);

        // Extract additional information
        extractCreditLimit(text, data);
        extractAvailableCredit(text, data);
        extractTransactions(text, data);

        logger.info("Successfully parsed HDFC statement. Valid: {}", data.isValid());
        return data;
    }

    @Override
    public boolean supports(String text) {
        if (text == null) return false;

        // Check for HDFC specific markers
        String upperText = text.toUpperCase();
        return upperText.contains("HDFC BANK") ||
                upperText.contains("HDFCBANK") ||
                (upperText.contains("HDFC") && upperText.contains("CREDIT CARD"));
    }

    @Override
    public String getIssuerName() {
        return ISSUER_NAME;
    }

    /**
     * Extract card last 4 digits
     */
    private void extractCardLastFourDigits(String text, StatementData data) {
        String cardNumber = PatternExtractor.extractFirst(text, CARD_NUMBER_PATTERN, 1);
        if (cardNumber != null) {
            data.setCardLastFourDigits(cardNumber);
            logger.debug("Extracted card last 4 digits: {}", cardNumber);
        } else {
            logger.warn("Could not extract card number");
        }
    }

    /**
     * Extract card variant/type
     */
    private void extractCardVariant(String text, StatementData data) {
        String variant = PatternExtractor.extractFirst(text, CARD_VARIANT_PATTERN, 1);
        if (variant != null) {
            data.setCardVariant(PatternExtractor.cleanText(variant));
            logger.debug("Extracted card variant: {}", variant);
        }
    }

    /**
     * Extract statement date
     */
    private void extractStatementDate(String text, StatementData data) {
        String dateStr = PatternExtractor.extractFirst(text, STATEMENT_DATE_PATTERN, 1);
        if (dateStr != null) {
            LocalDate date = DateParser.parseDate(dateStr);
            if (date != null) {
                data.setStatementDate(date);
                logger.debug("Extracted statement date: {}", date);
            }
        }
    }

    /**
     * Extract payment due date
     */
    private void extractPaymentDueDate(String text, StatementData data) {
        String dateStr = PatternExtractor.extractFirst(text, DUE_DATE_PATTERN, 1);
        if (dateStr != null) {
            LocalDate date = DateParser.parseDate(dateStr);
            if (date != null) {
                data.setPaymentDueDate(date);
                logger.debug("Extracted due date: {}", date);
            }
        }
    }

    /**
     * Extract total amount due
     */
    private void extractTotalAmountDue(String text, StatementData data) {
        String amountStr = PatternExtractor.extractFirst(text, TOTAL_DUE_PATTERN, 1);
        if (amountStr != null) {
            BigDecimal amount = AmountParser.parseAmount(amountStr);
            if (amount != null) {
                data.setTotalAmountDue(amount);
                logger.debug("Extracted total amount due: {}", amount);
            }
        }
    }

    /**
     * Extract credit limit
     */
    private void extractCreditLimit(String text, StatementData data) {
        String amountStr = PatternExtractor.extractFirst(text, CREDIT_LIMIT_PATTERN, 1);
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
    private void extractAvailableCredit(String text, StatementData data) {
        String amountStr = PatternExtractor.extractFirst(text, AVAILABLE_CREDIT_PATTERN, 1);
        if (amountStr != null) {
            BigDecimal amount = AmountParser.parseAmount(amountStr);
            if (amount != null) {
                data.setAvailableCredit(amount);
                logger.debug("Extracted available credit: {}", amount);
            }
        }
    }

    /**
     * Extract transactions
     */
    private void extractTransactions(String text, StatementData data) {
        Pattern pattern = Pattern.compile(TRANSACTION_PATTERN, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);

        int count = 0;
        while (matcher.find() && count < 50) { // Limit to 50 transactions
            try {
                String dateStr = matcher.group(1);
                String description = matcher.group(2).trim();
                String amountStr = matcher.group(3);

                LocalDate txnDate = DateParser.parseDate(dateStr);
                BigDecimal amount = AmountParser.parseAmount(amountStr);

                if (txnDate != null && amount != null) {
                    Transaction transaction = Transaction.builder()
                            .transactionDate(txnDate)
                            .description(description)
                            .amount(amount)
                            .type("DEBIT")
                            .build();

                    data.addTransaction(transaction);
                    count++;
                }
            } catch (Exception e) {
                logger.debug("Error parsing transaction: {}", e.getMessage());
            }
        }

        logger.info("Extracted {} transactions", count);
    }
}