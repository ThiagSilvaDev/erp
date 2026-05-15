package com.thiagsilvadev.erpapi.financial.internal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.model.AbstractBaseEntity;

@Entity
@Table(name = "financial_invoices")
public class Invoice extends AbstractBaseEntity {

    @Column(nullable = false)
    private UUID customerId;

    private UUID salesOrderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currencyCode;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.OPEN;

    protected Invoice() {}

    private Invoice(UUID customerId, UUID salesOrderId, BigDecimal amount, String currencyCode, LocalDate dueDate) {
        this.customerId = customerId;
        this.salesOrderId = salesOrderId;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.dueDate = dueDate;
    }

    public static Invoice create(
            UUID customerId, UUID salesOrderId, BigDecimal amount, String currencyCode, LocalDate dueDate) {
        return new Invoice(customerId, salesOrderId, amount, currencyCode, dueDate);
    }

    public void markPaid() {
        this.status = InvoiceStatus.PAID;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getSalesOrderId() {
        return salesOrderId;
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

    public InvoiceStatus getStatus() {
        return status;
    }
}
