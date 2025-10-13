package com.example.demo.parser.util;





import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmountParser {

    private static final Logger logger = LoggerFactory.getLogger(AmountParser.class);

    /**
     * Parses an amount string to BigDecimal
     * Handles various formats: ₹1,234.56, Rs. 1234.56, 1,234.56, etc.
     * @param amountString The amount string to parse
     * @return BigDecimal amount or null if parsing fails
     */
    public static BigDecimal parseAmount(String amountString) {
        if (amountString == null || amountString.trim().isEmpty()) {
            return null;
        }

        try {
            // Clean the amount string
            String cleaned = cleanAmountString(amountString);

            // Try parsing as double
            double value = Double.parseDouble(cleaned);
            BigDecimal amount = BigDecimal.valueOf(value);

            logger.debug("Parsed amount: {} -> {}", amountString, amount);
            return amount;

        } catch (NumberFormatException e) {
            logger.warn("Could not parse amount: {}", amountString);
            return null;
        }
    }

    /**
     * Cleans an amount string by removing currency symbols and formatting
     * @param amountString The original amount string
     * @return Cleaned numeric string
     */
    private static String cleanAmountString(String amountString) {
        return amountString
                .replaceAll("[₹$€£Rs.\\s]", "")  // Remove currency symbols and Rs.
                .replaceAll("Cr", "")             // Remove Cr (Credit)
                .replaceAll("Dr", "")             // Remove Dr (Debit)
                .replaceAll(",", "")              // Remove thousands separators
                .replaceAll("\\s+", "")           // Remove all whitespace
                .trim();
    }

    /**
     * Parses an amount in Indian format (e.g., 1,23,456.78)
     * @param amountString The amount string
     * @return BigDecimal amount or null
     */
    public static BigDecimal parseIndianAmount(String amountString) {
        if (amountString == null || amountString.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove Indian-style comma separators
            String cleaned = amountString
                    .replaceAll("[₹Rs.\\s]", "")
                    .replaceAll(",", "")
                    .trim();

            return new BigDecimal(cleaned);
        } catch (Exception e) {
            logger.warn("Could not parse Indian amount: {}", amountString);
            return null;
        }
    }

    /**
     * Checks if an amount is valid (not null and positive)
     * @param amount The amount to validate
     * @return true if valid
     */
    public static boolean isValidAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }

        // Amount must be positive and reasonable (less than 10 crores)
        BigDecimal maxAmount = new BigDecimal("100000000"); // 10 crores

        return amount.compareTo(BigDecimal.ZERO) > 0 &&
                amount.compareTo(maxAmount) < 0;
    }

    /**
     * Checks if an amount is valid (allows zero for available credit, etc.)
     * @param amount The amount to validate
     * @return true if valid (>= 0)
     */
    public static boolean isValidAmountOrZero(BigDecimal amount) {
        if (amount == null) {
            return false;
        }

        BigDecimal maxAmount = new BigDecimal("100000000"); // 10 crores

        return amount.compareTo(BigDecimal.ZERO) >= 0 &&
                amount.compareTo(maxAmount) < 0;
    }

    /**
     * Formats an amount as Indian currency string
     * @param amount The amount to format
     * @return Formatted string like "₹1,23,456.78"
     */
    public static String formatIndianCurrency(BigDecimal amount) {
        if (amount == null) return "₹0.00";

        // Convert to string with 2 decimal places
        String formatted = String.format("%.2f", amount);

        // Split into integer and decimal parts
        String[] parts = formatted.split("\\.");
        String integerPart = parts[0];
        String decimalPart = parts.length > 1 ? parts[1] : "00";

        // Add Indian-style comma separators
        StringBuilder result = new StringBuilder();
        int len = integerPart.length();

        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 2 == 0 && (len - i) != len) {
                result.append(",");
            } else if (i > 0 && i == len - 3) {
                result.append(",");
            }
            result.append(integerPart.charAt(i));
        }

        return "₹" + result.toString() + "." + decimalPart;
    }
}