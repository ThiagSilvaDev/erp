package com.thiagsilvadev.erpapi.financial.internal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;

@Entity
@Table(
        name = "financial_payments",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_financial_payment_payment_number", columnNames = "payment_number"))
public class Payment extends AbstractBaseEntity {

    @Column(name = "payment_number", nullable = false, updatable = false)
    private Long paymentNumber;

    @Column(name = "sales_order_id", nullable = false)
    private UUID salesOrderId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "destination_account_id")
    private UUID destinationAccountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    protected Payment() {}

    private Payment(
            Long paymentNumber,
            UUID salesOrderId,
            UUID customerId,
            UUID invoiceId,
            UUID destinationAccountId,
            BigDecimal amount,
            PaymentMethod method,
            PaymentStatus status,
            LocalDate dueDate,
            LocalDate paidAt) {
        this.paymentNumber = paymentNumber;
        this.salesOrderId = salesOrderId;
        this.customerId = customerId;
        this.invoiceId = invoiceId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.dueDate = dueDate;
        this.paidAt = paidAt;
    }

    public static Payment create(
            Long paymentNumber,
            UUID salesOrderId,
            UUID customerId,
            UUID invoiceId,
            UUID destinationAccountId,
            BigDecimal amount,
            PaymentMethod method,
            PaymentStatus status,
            LocalDate dueDate) {
        return new Payment(
                paymentNumber,
                salesOrderId,
                customerId,
                invoiceId,
                destinationAccountId,
                amount,
                method,
                status,
                dueDate,
                null);
    }

    public void markAsPaid(LocalDate paidAt) {
        this.status = PaymentStatus.PAID;
        this.paidAt = paidAt;
    }

    public Long getPaymentNumber() {
        return paymentNumber;
    }

    public UUID getSalesOrderId() {
        return salesOrderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getPaidAt() {
        return paidAt;
    }
}
