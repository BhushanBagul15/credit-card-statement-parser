package com.example.demo.parser.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.demo.parser.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents parsed credit card statement data
 * Contains the 5 key data points required for the assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementData {

    // ========== 5 KEY DATA POINTS (REQUIRED) ==========

    /**
     * 1. Card Last Four Digits
     */
    private String cardLastFourDigits;

    /**
     * 2. Card Variant/Type (e.g., "MoneyBack+", "Regalia")
     */
    private String cardVariant;

    /**
     * 3. Statement Date
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate statementDate;

    /**
     * 4. Payment Due Date
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDueDate;

    /**
     * 5. Total Amount Due
     */
    private BigDecimal totalAmountDue;

    // ========== ADDITIONAL FIELDS ==========

    private String issuerName;
    private String cardHolderName;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private BigDecimal minimumAmountDue;

    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    /**
     * Check if the parsed data contains all required fields
     * @return true if card number, amount, and due date are present
     */
    public boolean isValid() {
        return cardLastFourDigits != null &&
                totalAmountDue != null &&
                paymentDueDate != null;
    }

    /**
     * Add a transaction to the statement
     * @param transaction The transaction to add
     */
    public void addTransaction(Transaction transaction) {
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        this.transactions.add(transaction);
    }
}