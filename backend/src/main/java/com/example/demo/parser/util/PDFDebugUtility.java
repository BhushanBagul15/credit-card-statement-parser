package com.example.demo.parser.util;



import com.example.demo.parser.extractor.AdvancedPDFExtractor;
import com.example.demo.parser.extractor.PDFTextExtractor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
public class PDFDebugUtility {

    private static final Logger logger = LoggerFactory.getLogger(PDFDebugUtility.class);

    /**
     * Main method for standalone debugging
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java PDFDebugUtility <pdf-file-path>");
            System.out.println("Example: java PDFDebugUtility /path/to/statement.pdf");
            return;
        }

        String pdfPath = args[0];
        File pdfFile = new File(pdfPath);

        if (!pdfFile.exists()) {
            System.err.println("File not found: " + pdfPath);
            return;
        }

        try {
            analyzePDF(pdfFile);
        } catch (IOException e) {
            System.err.println("Error analyzing PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Comprehensive PDF analysis
     */
    public static void analyzePDF(File pdfFile) throws IOException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PDF ANALYSIS REPORT: " + pdfFile.getName());
        System.out.println("=".repeat(80));

        // Basic info
        analyzeBasicInfo(pdfFile);

        // Extract raw text
        System.out.println("\n--- RAW TEXT EXTRACTION ---");
        String rawText = PDFTextExtractor.extractText(pdfFile);
        System.out.println("Extracted " + rawText.length() + " characters");
        System.out.println("\nFirst 500 characters:");
        System.out.println(rawText.substring(0, Math.min(500, rawText.length())));

        // Save full text to file for inspection
        String outputPath = pdfFile.getAbsolutePath().replace(".pdf", "_extracted.txt");
        Files.write(Paths.get(outputPath), rawText.getBytes());
        System.out.println("\nFull text saved to: " + outputPath);

        // Layout-preserved extraction
        System.out.println("\n--- LAYOUT-PRESERVED EXTRACTION ---");
        String layoutText = AdvancedPDFExtractor.extractTextWithLayout(pdfFile);
        String layoutOutputPath = pdfFile.getAbsolutePath().replace(".pdf", "_layout.txt");
        Files.write(Paths.get(layoutOutputPath), layoutText.getBytes());
        System.out.println("Layout text saved to: " + layoutOutputPath);

        // Region extraction
        System.out.println("\n--- REGION EXTRACTION ---");
        Map<String, String> regions = AdvancedPDFExtractor.extractByRegions(pdfFile);
        for (Map.Entry<String, String> entry : regions.entrySet()) {
            System.out.println("\nRegion: " + entry.getKey());
            System.out.println("Content length: " + entry.getValue().length());
            System.out.println("First 200 chars: " +
                    entry.getValue().substring(0, Math.min(200, entry.getValue().length())));
        }

        // Text lines with positions
        System.out.println("\n--- TEXT LINES WITH POSITIONS ---");
        List<AdvancedPDFExtractor.TextLine> lines = AdvancedPDFExtractor.extractTextLines(pdfFile);
        System.out.println("Total lines: " + lines.size());
        System.out.println("\nFirst 20 lines:");
        for (int i = 0; i < Math.min(20, lines.size()); i++) {
            System.out.println(lines.get(i));
        }

        // Table detection
        System.out.println("\n--- TABLE DETECTION ---");
        List<List<String>> tables = AdvancedPDFExtractor.extractTables(pdfFile);
        System.out.println("Detected " + tables.size() + " table rows");
        System.out.println("\nFirst 10 rows:");
        for (int i = 0; i < Math.min(10, tables.size()); i++) {
            System.out.println("Row " + i + ": " + tables.get(i));
        }

        // Extract all dates
        System.out.println("\n--- ALL DATES FOUND ---");
        List<String> dates = AdvancedPDFExtractor.extractAllDates(rawText);
        System.out.println("Found " + dates.size() + " dates:");
        dates.forEach(date -> System.out.println("  - " + date));

        // Extract all amounts
        System.out.println("\n--- ALL AMOUNTS FOUND ---");
        List<String> amounts = AdvancedPDFExtractor.extractAllAmounts(rawText);
        System.out.println("Found " + amounts.size() + " amounts:");
        amounts.stream().limit(20).forEach(amt -> System.out.println("  - " + amt));

        // Search for common keywords
        System.out.println("\n--- KEYWORD SEARCH ---");
        searchKeywords(rawText);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("ANALYSIS COMPLETE");
        System.out.println("=".repeat(80));
    }

    /**
     * Analyze basic PDF properties
     */
    private static void analyzeBasicInfo(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            System.out.println("\n--- BASIC PDF INFO ---");
            System.out.println("File: " + pdfFile.getName());
            System.out.println("Size: " + (pdfFile.length() / 1024) + " KB");
            System.out.println("Pages: " + document.getNumberOfPages());

            if (document.getNumberOfPages() > 0) {
                PDPage firstPage = document.getPage(0);
                PDRectangle mediaBox = firstPage.getMediaBox();
                System.out.println("Page size: " + mediaBox.getWidth() + " x " + mediaBox.getHeight());
                System.out.println("Page orientation: " +
                        (mediaBox.getWidth() > mediaBox.getHeight() ? "Landscape" : "Portrait"));
            }

            // Check for encryption
            if (document.isEncrypted()) {
                System.out.println("WARNING: PDF is encrypted!");
            }
        }
    }

    /**
     * Search for common credit card statement keywords
     */
    private static void searchKeywords(String text) {
        String[] keywords = {
                "Card Number", "Card No", "Card ending",
                "Statement Date", "Bill Date",
                "Payment Due Date", "Due Date",
                "Total Amount Due", "Amount Due", "Outstanding",
                "Credit Limit", "Available Credit",
                "Minimum Payment", "Minimum Amount",
                "Card Type", "Product", "Card Variant",
                "Transaction", "Purchase", "Payment",
                "HDFC", "ICICI", "SBI", "Axis", "American Express"
        };

        for (String keyword : keywords) {
            if (text.toUpperCase().contains(keyword.toUpperCase())) {
                // Find context around the keyword
                int index = text.toUpperCase().indexOf(keyword.toUpperCase());
                int start = Math.max(0, index - 30);
                int end = Math.min(text.length(), index + keyword.length() + 50);
                String context = text.substring(start, end).replaceAll("\n", " ");
                System.out.println("✓ Found: " + keyword);
                System.out.println("  Context: ..." + context + "...");
            }
        }
    }

    /**
     * Quick test method to check if a PDF is parseable
     */
    public static boolean isParseablePDF(File pdfFile) {
        try {
            String text = PDFTextExtractor.extractText(pdfFile);
            return text != null && text.length() > 100;
        } catch (IOException e) {
            logger.error("Cannot parse PDF: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract and print specific field for debugging
     */
    public static void debugField(File pdfFile, String fieldName, String... keywords) throws IOException {
        System.out.println("\n--- DEBUGGING FIELD: " + fieldName + " ---");

        String rawText = PDFTextExtractor.extractText(pdfFile);
        String layoutText = AdvancedPDFExtractor.extractTextWithLayout(pdfFile);

        System.out.println("Searching for keywords: " + String.join(", ", keywords));

        String result = AdvancedPDFExtractor.findValueAfterKeyword(rawText, keywords);
        if (result != null) {
            System.out.println("✓ Found in raw text: " + result);
        } else {
            System.out.println("✗ Not found in raw text");
        }

        result = AdvancedPDFExtractor.findValueAfterKeyword(layoutText, keywords);
        if (result != null) {
            System.out.println("✓ Found in layout text: " + result);
        } else {
            System.out.println("✗ Not found in layout text");
        }
    }
}