package com.thiagsilvadev.erpapi.financial.internal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Entity
@Table(name = "financial_accounting_entries")
public class AccountingEntry extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "competence_date", nullable = false)
    private LocalDate competenceDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccountingEntryDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private AccountingEntrySourceType sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountingEntryStatus status = AccountingEntryStatus.POSTED;

    protected AccountingEntry() {}

    private AccountingEntry(
            UUID companyId,
            UUID accountId,
            LocalDate competenceDate,
            BigDecimal amount,
            String currencyCode,
            AccountingEntryDirection direction,
            AccountingEntrySourceType sourceType,
            UUID sourceId,
            String description) {
        this.companyId = Objects.requireNonNull(companyId, "Company ID is required");
        this.accountId = Objects.requireNonNull(accountId, "Account ID is required");
        this.competenceDate = Objects.requireNonNull(competenceDate, "Competence date is required");
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.amount = amount;
        this.currencyCode = Strings.required(currencyCode, "Currency code").toUpperCase();
        this.direction = Objects.requireNonNull(direction, "Direction is required");
        this.sourceType = Objects.requireNonNull(sourceType, "Source type is required");
        this.sourceId = sourceId;
        this.description = Strings.trimToNull(description);
    }

    public static AccountingEntry create(
            UUID companyId,
            UUID accountId,
            LocalDate competenceDate,
            BigDecimal amount,
            String currencyCode,
            AccountingEntryDirection direction,
            AccountingEntrySourceType sourceType,
            UUID sourceId,
            String description) {
        return new AccountingEntry(
                companyId,
                accountId,
                competenceDate,
                amount,
                currencyCode,
                direction,
                sourceType,
                sourceId,
                description);
    }

    public AccountingEntry reverse(LocalDate reverseCompetenceDate, String reverseDescription) {
        if (status != AccountingEntryStatus.POSTED) {
            throw new IllegalStateException("Only posted entries can be reversed");
        }
        this.status = AccountingEntryStatus.REVERSED;
        return create(
                companyId,
                accountId,
                reverseCompetenceDate != null ? reverseCompetenceDate : competenceDate,
                amount,
                currencyCode,
                direction.opposite(),
                AccountingEntrySourceType.ADJUSTMENT,
                sourceId,
                reverseDescription != null ? reverseDescription : "Reversal");
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public LocalDate getCompetenceDate() {
        return competenceDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public AccountingEntryDirection getDirection() {
        return direction;
    }

    public AccountingEntrySourceType getSourceType() {
        return sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public String getDescription() {
        return description;
    }

    public AccountingEntryStatus getStatus() {
        return status;
    }
}
