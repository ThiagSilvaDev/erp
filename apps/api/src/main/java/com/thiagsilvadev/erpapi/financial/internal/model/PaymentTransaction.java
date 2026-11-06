package com.thiagsilvadev.erpapi.financial.internal.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Entity
@Table(name = "financial_payment_transactions")
public class PaymentTransaction extends AbstractBaseEntity {

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private PaymentTransactionType type;

    @Embedded
    private Money amount;

    @Column(name = "provider_reference", length = 120)
    private String providerReference;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    protected PaymentTransaction() {}

    private PaymentTransaction(
            UUID paymentId, PaymentTransactionType type, Money amount, String providerReference, Instant occurredAt) {
        this.paymentId = DomainAssert.requireNotNull(paymentId, "Transaction payment");
        this.type = DomainAssert.requireNotNull(type, "Transaction type");
        this.amount = DomainAssert.requireNotNull(amount, "Transaction amount");
        this.providerReference = Strings.trimToNull(providerReference);
        this.occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }

    public static PaymentTransaction create(
            UUID paymentId, PaymentTransactionType type, Money amount, String providerReference, Instant occurredAt) {
        return new PaymentTransaction(paymentId, type, amount, providerReference, occurredAt);
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public PaymentTransactionType getType() {
        return type;
    }

    public Money getAmount() {
        return amount;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
