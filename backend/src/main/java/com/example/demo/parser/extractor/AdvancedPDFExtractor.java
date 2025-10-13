package com.example.demo.parser.extractor;



import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.TextPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced PDF extractor that handles complex layouts and table structures
 */
public class AdvancedPDFExtractor {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedPDFExtractor.class);

    /**
     * Extract text with better handling of layout
     */
    public static String extractTextWithLayout(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new LayoutPreservingPDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    /**
     * Extract text by regions for better accuracy
     */
    public static Map<String, String> extractByRegions(File pdfFile) throws IOException {
        Map<String, String> regions = new HashMap<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            if (document.getNumberOfPages() == 0) {
                return regions;
            }

            PDPage firstPage = document.getPage(0);
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);

            // Define regions (adjust based on typical statement layouts)
            Rectangle2D headerRegion = new Rectangle2D.Double(0, 0, 612, 150); // Top header
            Rectangle2D accountRegion = new Rectangle2D.Double(0, 150, 612, 300); // Account info
            Rectangle2D transactionRegion = new Rectangle2D.Double(0, 300, 612, 600); // Transactions

            stripper.addRegion("header", headerRegion);
            stripper.addRegion("account", accountRegion);
            stripper.addRegion("transactions", transactionRegion);

            stripper.extractRegions(firstPage);

            regions.put("header", stripper.getTextForRegion("header"));
            regions.put("account", stripper.getTextForRegion("account"));
            regions.put("transactions", stripper.getTextForRegion("transactions"));

            return regions;
        }
    }

    /**
     * Extract structured data as lines with position info
     */
    public static List<TextLine> extractTextLines(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            LineExtractingPDFTextStripper stripper = new LineExtractingPDFTextStripper();
            stripper.getText(document);
            return stripper.getTextLines();
        }
    }

    /**
     * Custom stripper that preserves layout better
     */
    private static class LayoutPreservingPDFTextStripper extends PDFTextStripper {

        public LayoutPreservingPDFTextStripper() throws IOException {
            super();
            setSortByPosition(true);
            setSpacingTolerance(1.5f);
            setAverageCharTolerance(0.3f);
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            // Preserve more spacing information
            if (text.trim().isEmpty()) {
                super.writeString(text, textPositions);
                return;
            }

            // Add extra processing for better layout preservation
            StringBuilder processed = new StringBuilder();
            float lastX = -1;

            for (TextPosition tp : textPositions) {
                float currentX = tp.getXDirAdj();

                // Detect large horizontal gaps (likely column separators)
                if (lastX != -1 && currentX - lastX > 50) {
                    processed.append("\t");
                }

                processed.append(tp.getUnicode());
                lastX = currentX + tp.getWidth();
            }

            super.writeString(processed.toString(), textPositions);
        }
    }

    /**
     * Text line with position information
     */
    public static class TextLine {
        private String text;
        private float x;
        private float y;
        private float fontSize;

        public TextLine(String text, float x, float y, float fontSize) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.fontSize = fontSize;
        }

        public String getText() { return text; }
        public float getX() { return x; }
        public float getY() { return y; }
        public float getFontSize() { return fontSize; }

        @Override
        public String toString() {
            return String.format("[%.1f,%.1f] (%.1fpt) %s", x, y, fontSize, text);
        }
    }

    /**
     * Stripper that extracts lines with position info
     */
    private static class LineExtractingPDFTextStripper extends PDFTextStripper {

        private List<TextLine> textLines = new ArrayList<>();
        private StringBuilder currentLine = new StringBuilder();
        private float currentX = 0;
        private float currentY = 0;
        private float currentFontSize = 0;

        public LineExtractingPDFTextStripper() throws IOException {
            super();
            setSortByPosition(true);
        }

        public List<TextLine> getTextLines() {
            return textLines;
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            if (textPositions.isEmpty()) {
                return;
            }

            TextPosition first = textPositions.get(0);
            float newY = first.getYDirAdj();

            // New line detected
            if (currentLine.length() > 0 && Math.abs(newY - currentY) > 5) {
                textLines.add(new TextLine(
                        currentLine.toString().trim(),
                        currentX,
                        currentY,
                        currentFontSize
                ));
                currentLine = new StringBuilder();
            }

            if (currentLine.length() == 0) {
                currentX = first.getXDirAdj();
                currentY = newY;
                currentFontSize = first.getFontSizeInPt();
            }

            currentLine.append(text);
        }

        @Override
        protected void writeLineSeparator() throws IOException {
            if (currentLine.length() > 0) {
                textLines.add(new TextLine(
                        currentLine.toString().trim(),
                        currentX,
                        currentY,
                        currentFontSize
                ));
                currentLine = new StringBuilder();
            }
            super.writeLineSeparator();
        }
    }

    /**
     * Extract tables from PDF (detects table structures)
     */
    public static List<List<String>> extractTables(File pdfFile) throws IOException {
        List<List<String>> tables = new ArrayList<>();
        List<TextLine> lines = extractTextLines(pdfFile);

        // Group lines by Y position (rows)
        Map<Integer, List<TextLine>> rowGroups = lines.stream()
                .collect(Collectors.groupingBy(line -> Math.round(line.getY())));

        // Sort by Y position (top to bottom)
        List<Integer> sortedYPositions = new ArrayList<>(rowGroups.keySet());
        Collections.sort(sortedYPositions);

        for (Integer y : sortedYPositions) {
            List<TextLine> row = rowGroups.get(y);

            // Sort cells by X position (left to right)
            row.sort(Comparator.comparing(TextLine::getX));

            // Extract cell text
            List<String> rowData = row.stream()
                    .map(TextLine::getText)
                    .filter(text -> !text.trim().isEmpty())
                    .collect(Collectors.toList());

            if (!rowData.isEmpty()) {
                tables.add(rowData);
            }
        }

        return tables;
    }

    /**
     * Smart keyword search with fuzzy matching
     */
    public static String findValueAfterKeyword(String text, String... keywords) {
        for (String keyword : keywords) {
            // Try exact match first
            String pattern = "(?i)" + keyword + "\\s*:?\\s*([^\n]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);

            if (m.find()) {
                return m.group(1).trim();
            }

            // Try fuzzy match (allow small variations)
            String fuzzyPattern = "(?i)" + keyword.replaceAll("\\s+", "\\\\s*") + "\\s*:?\\s*([^\n]+)";
            p = java.util.regex.Pattern.compile(fuzzyPattern);
            m = p.matcher(text);

            if (m.find()) {
                return m.group(1).trim();
            }
        }

        return null;
    }

    /**
     * Extract all amounts from text
     */
    public static List<String> extractAllAmounts(String text) {
        List<String> amounts = new ArrayList<>();
        String pattern = "(?:Rs\\.?|₹|INR)\\s*([\\d,]+\\.?\\d*)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(text);

        while (m.find()) {
            amounts.add(m.group(1));
        }

        return amounts;
    }

    /**
     * Extract all dates from text
     */
    public static List<String> extractAllDates(String text) {
        List<String> dates = new ArrayList<>();
        String[] patterns = {
                "\\d{2}[-/]\\d{2}[-/]\\d{4}",
                "\\d{2}[-/][A-Za-z]{3}[-/]\\d{4}",
                "\\d{2}\\s+[A-Za-z]{3}\\s+\\d{4}"
        };

        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);

            while (m.find()) {
                dates.add(m.group());
            }
        }

        return dates;
    }
}