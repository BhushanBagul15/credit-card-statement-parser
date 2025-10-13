package com.example.demo.parser.issuer;

import com.example.demo.parser.extractor.PDFTextExtractor;
import com.example.demo.parser.extractor.PatternExtractor;
import com.example.demo.parser.model.StatementData;
import com.creditcard.parser.model.Transaction;
import com.example.demo.parser.CreditCardParser;
import com.example.demo.parser.util.AmountParser;
import com.example.demo.parser.util.DateParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for ICICI Bank credit card statements
 */
public class ICICIParser implements CreditCardParser {

    private static final Logger logger = LoggerFactory.getLogger(ICICIParser.class);
    private static final String ISSUER_NAME = "ICICI Bank";

    // Patterns for ICICI statements
    private static final String CARD_NUMBER_PATTERN = "(?:Card\\s+Number|Card\\s+ending\\s+with)\\s*:?\\s*(?:X+\\s*)*?(\\d{4})";
    private static final String CARD_VARIANT_PATTERN = "(?:Card\\s+Type|Product)\\s*:?\\s*([A-Za-z\\s]+?)(?:\\n|Card)";
    private static final String STATEMENT_DATE_PATTERN = "(?:Statement\\s+Date|Date)\\s*:?\\s*([\\d]{2}[-/][A-Za-z]{3}[-/][\\d]{4})";
    private static final String DUE_DATE_PATTERN = "(?:Payment\\s+Due\\s+Date|Due\\s+Date)\\s*:?\\s*([\\d]{2}[-/][A-Za-z]{3}[-/][\\d]{4})";
    private static final String TOTAL_DUE_PATTERN = "(?:Total\\s+Amount\\s+Due|Amount\\s+Payable)\\s*:?\\s*(?:Rs\\.?|₹)?\\s*([\\d,]+\\.?\\d*)";
    private static final String CREDIT_LIMIT_PATTERN = "(?:Credit\\s+Limit)\\s*:?\\s*(?:Rs\\.?|₹)?\\s*([\\d,]+\\.?\\d*)";

    @Override
    public StatementData parse(File pdfFile) throws IOException {
        logger.info("Parsing ICICI Bank statement: {}", pdfFile.getName());

        String text = PDFTextExtractor.extractText(pdfFile);

        StatementData data = StatementData.builder()
                .issuerName(ISSUER_NAME)
                .build();

        // Extract 5 key data points
        extractCardLastFourDigits(text, data);
        extractCardVariant(text, data);
        extractStatementDate(text, data);
        extractPaymentDueDate(text, data);
        extractTotalAmountDue(text, data);

        // Additional information
        extractCreditLimit(text, data);

        logger.info("Successfully parsed ICICI statement. Valid: {}", data.isValid());
        return data;
    }

    @Override
    public boolean supports(String text) {
        if (text == null) return false;

        String upperText = text.toUpperCase();
        return upperText.contains("ICICI BANK") ||
                upperText.contains("ICICIBANK") ||
                (upperText.contains("ICICI") && upperText.contains("CREDIT CARD"));
    }

    @Override
    public String getIssuerName() {
        return ISSUER_NAME;
    }

    private void extractCardLastFourDigits(String text, StatementData data) {
        String cardNumber = PatternExtractor.extractFirst(text, CARD_NUMBER_PATTERN, 1);
        if (cardNumber != null) {
            data.setCardLastFourDigits(cardNumber);
            logger.debug("Extracted card last 4 digits: {}", cardNumber);
        }
    }

    private void extractCardVariant(String text, StatementData data) {
        String variant = PatternExtractor.extractFirst(text, CARD_VARIANT_PATTERN, 1);
        if (variant != null) {
            data.setCardVariant(PatternExtractor.cleanText(variant));
            logger.debug("Extracted card variant: {}", variant);
        }
    }

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
}