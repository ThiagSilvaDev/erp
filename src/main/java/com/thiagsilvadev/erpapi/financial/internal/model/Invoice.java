package com.thiagsilvadev.erpapi.financial.internal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;

@Entity
@Table(
        name = "financial_invoices",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_financial_invoice_sales_order", columnNames = "sales_order_id"),
            @UniqueConstraint(name = "uk_financial_invoice_invoice_number", columnNames = "invoice_number")
        })
public class Invoice extends AbstractBaseEntity {

    @Column(name = "invoice_number", nullable = false, updatable = false)
    private Long invoiceNumber;

    @Column(nullable = false)
    private UUID customerId;

    @Column(name = "sales_order_id")
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

    private Invoice(
            Long invoiceNumber,
            UUID customerId,
            UUID salesOrderId,
            BigDecimal amount,
            String currencyCode,
            LocalDate dueDate) {
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.salesOrderId = salesOrderId;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.dueDate = dueDate;
    }

    public static Invoice create(
            Long invoiceNumber,
            UUID customerId,
            UUID salesOrderId,
            BigDecimal amount,
            String currencyCode,
            LocalDate dueDate) {
        return new Invoice(invoiceNumber, customerId, salesOrderId, amount, currencyCode, dueDate);
    }

    public void markPaid() {
        if (status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled invoices cannot be paid");
        }
        this.status = InvoiceStatus.PAID;
    }

    public void cancel() {
        if (status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Paid invoices cannot be cancelled");
        }
        this.status = InvoiceStatus.CANCELLED;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public Long getInvoiceNumber() {
        return invoiceNumber;
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
