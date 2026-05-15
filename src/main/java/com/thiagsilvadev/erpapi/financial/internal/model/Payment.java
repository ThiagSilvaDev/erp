package com.thiagsilvadev.erpapi.financial.internal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.model.AbstractBaseEntity;

@Entity
@Table(name = "financial_payments")
public class Payment extends AbstractBaseEntity {

    @Column(nullable = false)
    private UUID invoiceId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paidAt;

    protected Payment() {}

    private Payment(UUID invoiceId, BigDecimal amount, LocalDate paidAt) {
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paidAt = paidAt;
    }

    public static Payment create(UUID invoiceId, BigDecimal amount, LocalDate paidAt) {
        return new Payment(invoiceId, amount, paidAt);
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
