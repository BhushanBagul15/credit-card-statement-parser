package com.example.demo.parser.extractor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import static org.apache.pdfbox.pdmodel.PDDocument.*;

/**
 * Utility class to extract text from PDF files using Apache PDFBox
 */
public class PDFTextExtractor {

    private static final Logger logger = LoggerFactory.getLogger(PDFTextExtractor.class);

    /**
     * Extracts all text from a PDF file
     * @param pdfFile The PDF file to extract text from
     * @return Extracted text as a string
     * @throws IOException if file reading fails
     */
    public static String extractText(File pdfFile) throws IOException {
        logger.info("Extracting text from PDF: {}", pdfFile.getName());

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            logger.info("Successfully extracted {} characters from PDF", text.length());
            return text;
        } catch (IOException e) {
            logger.error("Error extracting text from PDF: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts text from specific pages
     * @param pdfFile The PDF file
     * @param startPage Starting page number (1-indexed)
     * @param endPage Ending page number (1-indexed)
     * @return Extracted text
     * @throws IOException if file reading fails
     */
    public static String extractText(File pdfFile, int startPage, int endPage) throws IOException {
        logger.info("Extracting text from pages {}-{} of PDF: {}", startPage, endPage, pdfFile.getName());

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);

            String text = stripper.getText(document);
            logger.info("Successfully extracted {} characters from pages {}-{}",
                    text.length(), startPage, endPage);
            return text;
        } catch (IOException e) {
            logger.error("Error extracting text from PDF pages: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Gets the total number of pages in a PDF
     * @param pdfFile The PDF file
     * @return Number of pages
     * @throws IOException if file reading fails
     */
    public static int getPageCount(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            return document.getNumberOfPages();
        }
    }

    /**
     * Checks if a PDF file is valid and readable
     * @param pdfFile The PDF file to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidPDF(File pdfFile) {
        if (!pdfFile.exists() || !pdfFile.isFile()) {
            logger.warn("File does not exist or is not a file: {}", pdfFile.getAbsolutePath());
            return false;
        }

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            return document.getNumberOfPages() > 0;
        } catch (IOException e) {
            logger.warn("Invalid PDF file: {}", pdfFile.getName());
            return false;
        }
    }
}