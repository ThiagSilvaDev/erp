package com.thiagsilvadev.erpapi.financial.internal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;

@Entity
@Table(name = "financial_obligations")
public class FinancialObligation extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialObligationType type;

    private UUID invoiceId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currencyCode;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialObligationStatus status = FinancialObligationStatus.OPEN;

    protected FinancialObligation() {}

    private FinancialObligation(
            UUID companyId,
            FinancialObligationType type,
            UUID invoiceId,
            BigDecimal amount,
            String currencyCode,
            LocalDate dueDate) {
        this.companyId = DomainAssert.requireNotNull(companyId, "Company ID");
        this.type = type;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.dueDate = dueDate;
    }

    public static FinancialObligation receivable(
            UUID companyId, UUID invoiceId, BigDecimal amount, String currencyCode, LocalDate dueDate) {
        return new FinancialObligation(
                companyId, FinancialObligationType.RECEIVABLE, invoiceId, amount, currencyCode, dueDate);
    }

    public void settle() {
        this.status = FinancialObligationStatus.SETTLED;
    }

    public void cancel() {
        if (status != FinancialObligationStatus.SETTLED) {
            this.status = FinancialObligationStatus.CANCELLED;
        }
    }

    public FinancialObligationType getType() {
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

    public FinancialObligationStatus getStatus() {
        return status;
    }
}
