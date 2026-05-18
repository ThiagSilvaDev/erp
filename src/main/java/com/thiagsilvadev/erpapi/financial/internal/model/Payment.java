package com.thiagsilvadev.erpapi.financial.internal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;

@Entity
@Table(
        name = "financial_payments",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_financial_payment_payment_number", columnNames = "payment_number"))
public class Payment extends AbstractBaseEntity {

    @Column(name = "payment_number", nullable = false, updatable = false)
    private Long paymentNumber;

    @Column(nullable = false)
    private UUID invoiceId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paidAt;

    protected Payment() {}

    private Payment(Long paymentNumber, UUID invoiceId, BigDecimal amount, LocalDate paidAt) {
        this.paymentNumber = paymentNumber;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paidAt = paidAt;
    }

    public static Payment create(Long paymentNumber, UUID invoiceId, BigDecimal amount, LocalDate paidAt) {
        return new Payment(paymentNumber, invoiceId, amount, paidAt);
    }

    public Long getPaymentNumber() {
        return paymentNumber;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getPaidAt() {
        return paidAt;
    }
}
