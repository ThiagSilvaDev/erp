package com.thiagsilvadev.erpapi.company.internal.model;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;

@Entity
@Table(
        name = "companies",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_company_cnpj", columnNames = "cnpj"),
            @UniqueConstraint(name = "uk_company_company_code", columnNames = "company_code")
        })
public class Company extends AbstractBaseEntity {

    @Column(name = "company_code", nullable = false, updatable = false)
    private Long companyCode;

    @Column(nullable = false)
    private String legalName;

    private String tradeName;

    @Embedded
    @AttributeOverride(name = "number", column = @Column(name = "cnpj", length = 14, nullable = false))
    private Cnpj cnpj;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyStatus status = CompanyStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyType type;

    @Embedded
    private Address address;

    @Embedded
    private Contact contact;

    protected Company() {}

    private Company(
            Long companyCode,
            String legalName,
            String tradeName,
            Cnpj cnpj,
            CompanyType type,
            Address address,
            Contact contact) {
        this.companyCode = DomainAssert.requireRequired(companyCode, "Company code");
        this.legalName = Strings.required(legalName, "Legal name");
        this.tradeName = Strings.trimToNull(tradeName);
        this.cnpj = DomainAssert.requireRequired(cnpj, "CNPJ");
        this.type = DomainAssert.requireRequired(type, "Company type");
        this.address = address;
        this.contact = contact;
    }

    public static Company create(
            Long companyCode,
            String legalName,
            String tradeName,
            Cnpj cnpj,
            CompanyType type,
            Address address,
            Contact contact) {
        return new Company(companyCode, legalName, tradeName, cnpj, type, address, contact);
    }

    public void updateNames(String legalName, String tradeName) {
        this.legalName = Strings.required(legalName, "Legal name");
        this.tradeName = Strings.trimToNull(tradeName);
    }

    public void changeType(CompanyType type) {
        this.type = DomainAssert.requireNotNull(type, "Company type");
    }

    public void changeAddress(Address address) {
        this.address = DomainAssert.requireNotNull(address, "Address");
    }

    public void changeContact(Contact contact) {
        this.contact = DomainAssert.requireNotNull(contact, "Contact");
    }

    public void activate() {
        this.status = CompanyStatus.ACTIVE;
    }

    public void inactivate() {
        this.status = CompanyStatus.INACTIVE;
    }

    public String getLegalName() {
        return legalName;
    }

    public Long getCompanyCode() {
        return companyCode;
    }

    public String getTradeName() {
        return tradeName;
    }

    public Cnpj getCnpj() {
        return cnpj;
    }

    public CompanyStatus getStatus() {
        return status;
    }

    public CompanyType getType() {
        return type;
    }

    public Address getAddress() {
        return address;
    }

    public Contact getContact() {
        return contact;
    }
}
