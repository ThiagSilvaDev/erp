package com.thiagsilvadev.erpapi.financial.internal.model;

import java.util.Objects;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;

@Entity
@Table(
        name = "financial_accounts",
        uniqueConstraints = @UniqueConstraint(name = "uk_financial_account_name", columnNames = "name"))
public class FinancialAccount extends AbstractBaseEntity {

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

    private FinancialAccount(String name, FinancialAccountType type, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Financial account name is required");
        }
        this.name = name.trim();
        this.type = Objects.requireNonNull(type, "Financial account type is required");
        this.description = description == null ? null : description.trim();
    }

    public static FinancialAccount create(String name, FinancialAccountType type, String description) {
        return new FinancialAccount(name, type, description);
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
