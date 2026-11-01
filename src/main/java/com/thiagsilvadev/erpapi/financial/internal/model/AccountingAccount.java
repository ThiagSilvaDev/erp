package com.thiagsilvadev.erpapi.financial.internal.model;

import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Entity
@Table(
        name = "financial_accounting_accounts",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_fin_accounting_account_company_code",
                        columnNames = {"company_id", "code"}))
public class AccountingAccount extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "parent_id")
    private UUID parentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountingAccountType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "system_role", length = 40)
    private AccountingSystemRole systemRole;

    @Column(name = "accepts_postings", nullable = false)
    private boolean acceptsPostings = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinancialResourceStatus status = FinancialResourceStatus.ACTIVE;

    protected AccountingAccount() {}

    private AccountingAccount(
            UUID companyId,
            String code,
            String name,
            String description,
            UUID parentId,
            AccountingAccountType type,
            AccountingSystemRole systemRole,
            boolean acceptsPostings) {
        this.companyId = DomainAssert.requireNotNull(companyId, "Company ID");
        this.code = Strings.required(code, "Accounting account code");
        this.name = Strings.required(name, "Accounting account name");
        this.description = Strings.trimToNull(description);
        this.parentId = parentId;
        this.type = DomainAssert.requireNotNull(type, "Accounting account type");
        this.systemRole = systemRole;
        this.acceptsPostings = acceptsPostings;
    }

    public static AccountingAccount create(
            UUID companyId,
            String code,
            String name,
            String description,
            UUID parentId,
            AccountingAccountType type,
            AccountingSystemRole systemRole,
            boolean acceptsPostings) {
        return new AccountingAccount(companyId, code, name, description, parentId, type, systemRole, acceptsPostings);
    }

    public void deactivate() {
        this.status = FinancialResourceStatus.INACTIVE;
    }

    public void activate() {
        this.status = FinancialResourceStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == FinancialResourceStatus.ACTIVE;
    }

    public boolean canAcceptPostings() {
        return isActive() && acceptsPostings;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getParentId() {
        return parentId;
    }

    public AccountingAccountType getType() {
        return type;
    }

    public AccountingSystemRole getSystemRole() {
        return systemRole;
    }

    public boolean isAcceptsPostings() {
        return acceptsPostings;
    }

    public FinancialResourceStatus getStatus() {
        return status;
    }
}
