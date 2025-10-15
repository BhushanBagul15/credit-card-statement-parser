package com.example.demo.parser.controller;


import com.example.demo.parser.model.StatementData;
import com.example.demo.parser.service.ParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;


@CrossOrigin(origins = "http://localhost:5174")
@RestController
@RequestMapping("/api/statements")
@Tag(name = "Statement Parser", description = "APIs for parsing credit card statements")
public class StatementParserController {

    private static final Logger logger = LoggerFactory.getLogger(StatementParserController.class);

    @Autowired
    private ParserService parserService;


    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if API is running")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Credit Card Statement Parser");
        response.put("version", "1.0");
        return ResponseEntity.ok(response);
    }


    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Parse Statement", description = "Upload and parse a credit card statement PDF")
    public ResponseEntity<?> parseStatement(@RequestParam("file") MultipartFile file) {

        logger.info("Received file upload request: {}", file.getOriginalFilename());


        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("No file uploaded"));
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Only PDF files are supported"));
        }

        try {

            File tempFile = saveTemporaryFile(file);


            StatementData parsedData = parserService.parseStatement(tempFile);


            tempFile.delete();

            if (parsedData == null || !parsedData.isValid()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(createErrorResponse("Failed to parse statement. Unsupported format."));
            }

            logger.info("Successfully parsed statement from {}", parsedData.getIssuerName());
            return ResponseEntity.ok(parsedData);

        } catch (Exception e) {
            logger.error("Error parsing statement: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error processing file: " + e.getMessage()));
        }
    }


    @GetMapping("/supported-issuers")
    @Operation(summary = "Supported Issuers", description = "Get list of supported credit card issuers")
    public ResponseEntity<?> getSupportedIssuers() {
        Map<String, Object> response = new HashMap<>();
        response.put("issuers", new String[]{
                "HDFC Bank",
                "ICICI Bank",
                "SBI Card",
                "Axis Bank"

        });
        response.put("count", 5);
        return ResponseEntity.ok(response);
    }

    /**
     * Debug endpoint - Extract raw text from PDF
     */
    @PostMapping(value = "/debug", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Debug PDF", description = "Extract and return raw text from PDF for debugging")
    public ResponseEntity<?> debugPDF(@RequestParam("file") MultipartFile file) {
        try {
            File tempFile = saveTemporaryFile(file);
            String rawText = com.example.demo.parser.extractor.PDFTextExtractor.extractText(tempFile);
            String layoutText = com.example.demo.parser.extractor.AdvancedPDFExtractor.extractTextWithLayout(tempFile);

            Map<String, Object> debug = new HashMap<>();
            debug.put("fileName", file.getOriginalFilename());
            debug.put("rawTextLength", rawText.length());
            debug.put("rawTextPreview", rawText.substring(0, Math.min(1000, rawText.length())));
            debug.put("layoutTextPreview", layoutText.substring(0, Math.min(1000, layoutText.length())));
            debug.put("issuerDetected", parserService.detectIssuer(rawText));

            tempFile.delete();
            return ResponseEntity.ok(debug);

        } catch (Exception e) {
            logger.error("Error debugging PDF: {}", e.getMessage());
            return ResponseEntity.status(500).body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Save uploaded file temporarily
     */
    private File saveTemporaryFile(MultipartFile file) throws IOException {
        Path tempDir = Files.createTempDirectory("credit-card-parser");
        Path tempFile = tempDir.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        return tempFile.toFile();
    }

    /**
     * Create error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", java.time.Instant.now().toString());
        return error;
    }
}