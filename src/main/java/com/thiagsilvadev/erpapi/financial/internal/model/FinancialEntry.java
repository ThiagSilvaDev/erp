package com.thiagsilvadev.erpapi.financial.internal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.model.AbstractBaseEntity;

@Entity
@Table(name = "financial_entries")
public class FinancialEntry extends AbstractBaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type;

    private UUID invoiceId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currencyCode;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryStatus status = EntryStatus.OPEN;

    protected FinancialEntry() {}

    private FinancialEntry(EntryType type, UUID invoiceId, BigDecimal amount, String currencyCode, LocalDate dueDate) {
        this.type = type;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.dueDate = dueDate;
    }

    public static FinancialEntry receivable(UUID invoiceId, BigDecimal amount, String currencyCode, LocalDate dueDate) {
        return new FinancialEntry(EntryType.RECEIVABLE, invoiceId, amount, currencyCode, dueDate);
    }

    public void settle() {
        this.status = EntryStatus.SETTLED;
    }

    public EntryType getType() {
        return type;
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

    public EntryStatus getStatus() {
        return status;
    }
}
