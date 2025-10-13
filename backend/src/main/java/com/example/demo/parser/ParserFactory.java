package com.example.demo.parser;

import com.example.demo.parser.issuer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to get the appropriate parser based on statement content
 */
public class ParserFactory {

    private static final Logger logger = LoggerFactory.getLogger(ParserFactory.class);
    private static final List<CreditCardParser> parsers = new ArrayList<>();

    // Register all available parsers
    static {
        parsers.add(new ImprovedHDFCParser());
        parsers.add(new ICICIParser());
        parsers.add(new SBIParser());
        parsers.add(new AxisParser());
        parsers.add(new AmexParser());

        logger.info("Registered {} credit card parsers", parsers.size());
    }

    /**
     * Get the appropriate parser for the given statement text
     * @param statementText The extracted text from PDF
     * @return Appropriate parser or null if no parser supports the format
     */
    public static CreditCardParser getParser(String statementText) {
        if (statementText == null || statementText.trim().isEmpty()) {
            logger.warn("Empty statement text provided");
            return null;
        }

        for (CreditCardParser parser : parsers) {
            if (parser.supports(statementText)) {
                logger.info("Found matching parser: {}", parser.getIssuerName());
                return parser;
            }
        }

        logger.warn("No suitable parser found for the statement");
        return null;
    }

    /**
     * Detect the issuer from statement text
     * @param statementText The extracted text from PDF
     * @return Issuer name or "Unknown"
     */
    public static String detectIssuer(String statementText) {
        CreditCardParser parser = getParser(statementText);
        return parser != null ? parser.getIssuerName() : "Unknown";
    }

    /**
     * Get all registered parsers
     * @return List of all parsers
     */
    public static List<CreditCardParser> getAllParsers() {
        return new ArrayList<>(parsers);
    }

    /**
     * Get list of supported issuers
     * @return Array of issuer names
     */
    public static String[] getSupportedIssuers() {
        return parsers.stream()
                .map(CreditCardParser::getIssuerName)
                .toArray(String[]::new);
    }
}