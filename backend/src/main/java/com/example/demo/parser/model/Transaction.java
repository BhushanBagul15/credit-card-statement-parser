package com.creditcard.parser.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a single transaction in the credit card statement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate postingDate;

    private String description;
    private String merchantName;
    private BigDecimal amount;
    private String type; // DEBIT, CREDIT, FEE, etc.

    /**
     * Constructor with essential fields
     */
    public Transaction(LocalDate transactionDate, String description, BigDecimal amount) {
        this.transactionDate = transactionDate;
        this.description = description;
        this.amount = amount;
    }
}