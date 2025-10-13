package com.example.demo.parser.extractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for parsing dates from various formats
 */
public class DateParser {

    private static final Logger logger = LoggerFactory.getLogger(DateParser.class);

    // Common date formats found in credit card statements
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd MMM yyyy"),
            DateTimeFormatter.ofPattern("dd-MMM-yyyy"),
            DateTimeFormatter.ofPattern("dd MMMM yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("d MMM yyyy"),
            DateTimeFormatter.ofPattern("d-MMM-yyyy"),
            DateTimeFormatter.ofPattern("MMM dd, yyyy"),
            DateTimeFormatter.ofPattern("MMMM dd, yyyy"),
            DateTimeFormatter.ofPattern("dd-MMM-yy"),
            DateTimeFormatter.ofPattern("dd/MMM/yyyy")
    );

    /**
     * Parses a date string using multiple common formats
     * @param dateString The date string to parse
     * @return LocalDate object or null if parsing fails
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        String cleanDate = dateString.trim();

        // Try each formatter
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(cleanDate, formatter);
                logger.debug("Successfully parsed date: {} -> {}", dateString, date);
                return date;
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }

        logger.warn("Could not parse date: {}", dateString);
        return null;
    }

    /**
     * Parses a date with a specific format
     * @param dateString The date string
     * @param format The date format pattern
     * @return LocalDate object or null if parsing fails
     */
    public static LocalDate parseDate(String dateString, String format) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return LocalDate.parse(dateString.trim(), formatter);
        } catch (Exception e) {
            logger.warn("Could not parse date '{}' with format '{}': {}",
                    dateString, format, e.getMessage());
            return null;
        }
    }

    /**
     * Extracts and parses a date from a longer text string
     * @param text The text containing a date
     * @return LocalDate object or null
     */
    public static LocalDate extractAndParseDate(String text) {
        if (text == null) return null;

        // Try to find date patterns in the text
        String[] patterns = {
                "\\d{2}-\\d{2}-\\d{4}",
                "\\d{2}/\\d{2}/\\d{4}",
                "\\d{2}\\s+[A-Za-z]{3}\\s+\\d{4}",
                "\\d{2}-[A-Za-z]{3}-\\d{4}",
                "\\d{2}-[A-Za-z]{3}-\\d{2}"
        };

        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = p.matcher(text);

            if (matcher.find()) {
                String dateStr = matcher.group();
                LocalDate date = parseDate(dateStr);
                if (date != null) {
                    return date;
                }
            }
        }

        return null;
    }

    /**
     * Checks if a date is valid (not null and reasonable)
     * @param date The date to validate
     * @return true if valid
     */
    public static boolean isValidDate(LocalDate date) {
        if (date == null) return false;

        // Check if date is within reasonable range (past 10 years to future 1 year)
        LocalDate now = LocalDate.now();
        LocalDate tenYearsAgo = now.minusYears(10);
        LocalDate oneYearFromNow = now.plusYears(1);

        return date.isAfter(tenYearsAgo) && date.isBefore(oneYearFromNow);
    }
}