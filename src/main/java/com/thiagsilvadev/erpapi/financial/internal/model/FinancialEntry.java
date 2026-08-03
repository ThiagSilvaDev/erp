package com.thiagsilvadev.erpapi.financial.internal.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Entity
@Table(name = "financial_entries")
public class FinancialEntry extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FinancialEntryType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FinancialEntrySource source;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(length = 255)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected FinancialEntry() {}

    private FinancialEntry(
            UUID companyId,
            UUID accountId,
            FinancialEntryType type,
            FinancialEntrySource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description) {
        this.companyId = Objects.requireNonNull(companyId, "Company ID is required");
        this.accountId = Objects.requireNonNull(accountId, "Account ID is required");
        this.type = Objects.requireNonNull(type, "Entry type is required");
        this.source = Objects.requireNonNull(source, "Entry source is required");
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.amount = amount;
        this.currencyCode =
                Strings.required(currencyCode, "Currency code is required").toUpperCase();
        this.referenceId = referenceId;
        this.description = Strings.trimToNull(description);
        this.occurredAt = Instant.now();
    }

    public static FinancialEntry income(
            UUID companyId,
            UUID accountId,
            FinancialEntrySource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description) {
        return new FinancialEntry(
                companyId,
                accountId,
                FinancialEntryType.INCOME,
                source,
                amount,
                currencyCode,
                referenceId,
                description);
    }

    public static FinancialEntry expense(
            UUID companyId,
            UUID accountId,
            FinancialEntrySource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description) {
        return new FinancialEntry(
                companyId,
                accountId,
                FinancialEntryType.EXPENSE,
                source,
                amount,
                currencyCode,
                referenceId,
                description);
    }

    public static FinancialEntry transferIn(
            UUID companyId,
            UUID accountId,
            FinancialEntrySource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description) {
        return new FinancialEntry(
                companyId,
                accountId,
                FinancialEntryType.TRANSFER_IN,
                source,
                amount,
                currencyCode,
                referenceId,
                description);
    }

    public static FinancialEntry transferOut(
            UUID companyId,
            UUID accountId,
            FinancialEntrySource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description) {
        return new FinancialEntry(
                companyId,
                accountId,
                FinancialEntryType.TRANSFER_OUT,
                source,
                amount,
                currencyCode,
                referenceId,
                description);
    }

    public static FinancialEntry adjustment(
            UUID companyId,
            UUID accountId,
            FinancialEntrySource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description) {
        return new FinancialEntry(
                companyId,
                accountId,
                FinancialEntryType.ADJUSTMENT,
                source,
                amount,
                currencyCode,
                referenceId,
                description);
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public FinancialEntryType getType() {
        return type;
    }

    public FinancialEntrySource getSource() {
        return source;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
