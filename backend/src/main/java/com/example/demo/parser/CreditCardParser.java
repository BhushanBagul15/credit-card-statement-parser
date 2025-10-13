package com.example.demo.parser;

import com.example.demo.parser.model.StatementData;

import java.io.File;
import java.io.IOException;

/**
 * Interface for credit card statement parsers
 * Each issuer will implement this interface with their specific parsing logic
 */
public interface CreditCardParser {

    /**
     * Parse a credit card statement PDF file
     * @param pdfFile The PDF file to parse
     * @return Parsed statement data
     * @throws IOException if file reading fails
     */
    StatementData parse(File pdfFile) throws IOException;

    /**
     * Check if this parser can handle the given statement
     * @param text The extracted text from PDF
     * @return true if this parser supports the statement format
     */
    boolean supports(String text);

    /**
     * Get the issuer name
     * @return The name of the credit card issuer
     */
    String getIssuerName();
}