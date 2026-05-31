package com.thiagsilvadev.erpapi.financial.internal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerEntryType type;

    private UUID invoiceId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currencyCode;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerEntryStatus status = LedgerEntryStatus.OPEN;

    protected LedgerEntry() {}

    private LedgerEntry(
            UUID companyId,
            LedgerEntryType type,
            UUID invoiceId,
            BigDecimal amount,
            String currencyCode,
            LocalDate dueDate) {
        this.companyId = java.util.Objects.requireNonNull(companyId, "Company ID is required");
        this.type = type;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.dueDate = dueDate;
    }

    public static LedgerEntry receivable(
            UUID companyId, UUID invoiceId, BigDecimal amount, String currencyCode, LocalDate dueDate) {
        return new LedgerEntry(companyId, LedgerEntryType.RECEIVABLE, invoiceId, amount, currencyCode, dueDate);
    }

    public void settle() {
        this.status = LedgerEntryStatus.SETTLED;
    }

    public void cancel() {
        if (status != LedgerEntryStatus.SETTLED) {
            this.status = LedgerEntryStatus.CANCELLED;
        }
    }

    public LedgerEntryType getType() {
        return type;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LedgerEntryStatus getStatus() {
        return status;
    }
}
