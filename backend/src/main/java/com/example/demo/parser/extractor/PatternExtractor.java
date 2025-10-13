package com.example.demo.parser.extractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for pattern-based text extraction using regex
 */
public class PatternExtractor {

    private static final Logger logger = LoggerFactory.getLogger(PatternExtractor.class);

    /**
     * Extracts the first match of a pattern from text
     * @param text The text to search in
     * @param pattern The regex pattern
     * @return First match or null if not found
     */
    public static String extractFirst(String text, String pattern) {
        return extractFirst(text, pattern, 1);
    }

    /**
     * Extracts the first match of a pattern from text with specific group
     * @param text The text to search in
     * @param pattern The regex pattern
     * @param group The capture group number (0 for entire match)
     * @return First match or null if not found
     */
    public static String extractFirst(String text, String pattern, int group) {
        try {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher matcher = p.matcher(text);

            if (matcher.find()) {
                String result = matcher.group(group);
                logger.debug("Pattern matched: {} -> {}", pattern, result);
                return result != null ? result.trim() : null;
            }
        } catch (Exception e) {
            logger.error("Error extracting pattern '{}': {}", pattern, e.getMessage());
        }

        return null;
    }

    /**
     * Extracts all matches of a pattern from text
     * @param text The text to search in
     * @param pattern The regex pattern
     * @return List of all matches
     */
    public static List<String> extractAll(String text, String pattern) {
        return extractAll(text, pattern, 1);
    }

    /**
     * Extracts all matches of a pattern from text with specific group
     * @param text The text to search in
     * @param pattern The regex pattern
     * @param group The capture group number
     * @return List of all matches
     */
    public static List<String> extractAll(String text, String pattern, int group) {
        List<String> results = new ArrayList<>();

        try {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher matcher = p.matcher(text);

            while (matcher.find()) {
                String match = matcher.group(group);
                if (match != null) {
                    results.add(match.trim());
                }
            }

            logger.debug("Pattern matched {} times: {}", results.size(), pattern);
        } catch (Exception e) {
            logger.error("Error extracting all patterns '{}': {}", pattern, e.getMessage());
        }

        return results;
    }

    /**
     * Checks if a pattern exists in text
     * @param text The text to search in
     * @param pattern The regex pattern
     * @return true if pattern found, false otherwise
     */
    public static boolean matches(String text, String pattern) {
        try {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            return p.matcher(text).find();
        } catch (Exception e) {
            logger.error("Error checking pattern '{}': {}", pattern, e.getMessage());
            return false;
        }
    }

    /**
     * Extracts text between two markers
     * @param text The text to search in
     * @param startMarker Starting marker
     * @param endMarker Ending marker
     * @return Text between markers or null
     */
    public static String extractBetween(String text, String startMarker, String endMarker) {
        try {
            String pattern = Pattern.quote(startMarker) + "(.*?)" + Pattern.quote(endMarker);
            return extractFirst(text, pattern, 1);
        } catch (Exception e) {
            logger.error("Error extracting between markers: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Cleans extracted text by removing extra whitespace
     * @param text The text to clean
     * @return Cleaned text
     */
    public static String cleanText(String text) {
        if (text == null) return null;
        return text.replaceAll("\\s+", " ").trim();
    }
}