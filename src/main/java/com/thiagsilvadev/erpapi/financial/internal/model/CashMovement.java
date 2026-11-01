package com.thiagsilvadev.erpapi.financial.internal.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Entity
@Table(name = "financial_cash_movements")
public class CashMovement extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CashMovementType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CashMovementSource source;

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

    protected CashMovement() {}

    private CashMovement(
            UUID companyId,
            UUID accountId,
            CashMovementType type,
            CashMovementSource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description) {
        this.companyId = DomainAssert.requireNotNull(companyId, "Company ID");
        this.accountId = DomainAssert.requireNotNull(accountId, "Account ID");
        this.type = DomainAssert.requireNotNull(type, "Entry type");
        this.source = DomainAssert.requireNotNull(source, "Entry source");
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.amount = amount;
        this.currencyCode = Strings.required(currencyCode, "Currency code").toUpperCase();
        this.referenceId = referenceId;
        this.description = Strings.trimToNull(description);
        this.occurredAt = Instant.now();
    }

    public static CashMovement income(
            UUID companyId,
            UUID accountId,
            CashMovementSource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description) {
        return new CashMovement(
                companyId, accountId, CashMovementType.INCOME, source, amount, currencyCode, referenceId, description);
    }

    public static CashMovement expense(
            UUID companyId,
            UUID accountId,
            CashMovementSource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description) {
        return new CashMovement(
                companyId, accountId, CashMovementType.EXPENSE, source, amount, currencyCode, referenceId, description);
    }

    public static CashMovement transferIn(
            UUID companyId,
            UUID accountId,
            CashMovementSource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description) {
        return new CashMovement(
                companyId,
                accountId,
                CashMovementType.TRANSFER_IN,
                source,
                amount,
                currencyCode,
                referenceId,
                description);
    }

    public static CashMovement transferOut(
            UUID companyId,
            UUID accountId,
            CashMovementSource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description) {
        return new CashMovement(
                companyId,
                accountId,
                CashMovementType.TRANSFER_OUT,
                source,
                amount,
                currencyCode,
                referenceId,
                description);
    }

    public static CashMovement adjustment(
            UUID companyId,
            UUID accountId,
            CashMovementSource source,
            BigDecimal amount,
            String currencyCode,
            UUID referenceId,
            String description) {
        return new CashMovement(
                companyId,
                accountId,
                CashMovementType.ADJUSTMENT,
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

    public CashMovementType getType() {
        return type;
    }

    public CashMovementSource getSource() {
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
