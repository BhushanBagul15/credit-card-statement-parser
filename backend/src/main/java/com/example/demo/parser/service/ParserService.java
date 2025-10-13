package com.example.demo.parser.service;

import com.example.demo.parser.extractor.PDFTextExtractor;
import com.example.demo.parser.model.StatementData;
import com.example.demo.parser.CreditCardParser;
import com.example.demo.parser.ParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Service for parsing credit card statements
 */
@Service
public class ParserService {

    private static final Logger logger = LoggerFactory.getLogger(ParserService.class);

    /**
     * Parse a credit card statement PDF file
     * @param pdfFile The PDF file to parse
     * @return Parsed statement data
     * @throws IOException if file reading fails
     */
    public StatementData parseStatement(File pdfFile) throws IOException {
        logger.info("Starting to parse statement: {}", pdfFile.getName());

        // Validate PDF
        if (!PDFTextExtractor.isValidPDF(pdfFile)) {
            throw new IOException("Invalid PDF file");
        }

        // Extract text
        String text = PDFTextExtractor.extractText(pdfFile);

        // Detect issuer and get appropriate parser
        CreditCardParser parser = ParserFactory.getParser(text);

        if (parser == null) {
            logger.warn("No suitable parser found for the statement");
            return null;
        }

        // Parse the statement
        StatementData data = parser.parse(pdfFile);

        logger.info("Successfully parsed statement from: {}", data.getIssuerName());
        return data;
    }

    /**
     * Detect the issuer from PDF text
     * @param text The extracted text
     * @return Issuer name or "Unknown"
     */
    public String detectIssuer(String text) {
        return ParserFactory.detectIssuer(text);
    }
}
