package com.thiagsilvadev.erpapi.financial.internal.model;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;

@Entity
@Table(
        name = "financial_accounts",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_financial_account_company_name",
                        columnNames = {"company_id", "name"}))
public class FinancialAccount extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FinancialAccountType type;

    @Column(length = 120)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinancialResourceStatus status = FinancialResourceStatus.ACTIVE;

    protected FinancialAccount() {}

    private FinancialAccount(UUID companyId, String name, FinancialAccountType type, String description) {
        this.companyId = Objects.requireNonNull(companyId, "Company ID is required");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Financial account name is required");
        }
        this.name = name.trim();
        this.type = Objects.requireNonNull(type, "Financial account type is required");
        this.description = description == null ? null : description.trim();
    }

    public static FinancialAccount create(UUID companyId, String name, FinancialAccountType type, String description) {
        return new FinancialAccount(companyId, name, type, description);
    }

    public void deactivate() {
        this.status = FinancialResourceStatus.INACTIVE;
    }

    public void activate() {
        this.status = FinancialResourceStatus.ACTIVE;
    }

    public String getName() {
        return name;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public FinancialAccountType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public FinancialResourceStatus getStatus() {
        return status;
    }
}
