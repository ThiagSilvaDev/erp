package com.thiagsilvadev.erpapi.company.internal.model;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.model.Cnpj;
import com.thiagsilvadev.erpapi.common.model.Contact;
import com.thiagsilvadev.erpapi.common.model.address.Address;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;

@Entity
@Table(
        name = "companies",
        uniqueConstraints = {@UniqueConstraint(name = "uk_company_cnpj", columnNames = "cnpj")})
public class Company extends AbstractBaseEntity {

    @Column(nullable = false)
    private String legalName;

    private String tradeName;

    @Embedded
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

    private Company(String legalName, String tradeName, Cnpj cnpj, CompanyType type, Address address, Contact contact) {
        this.legalName = legalName;
        this.tradeName = tradeName;
        this.cnpj = cnpj;
        this.type = type;
        this.address = address;
        this.contact = contact;
    }

    public static Company create(
            String legalName, String tradeName, Cnpj cnpj, CompanyType type, Address address, Contact contact) {
        return new Company(legalName, tradeName, cnpj, type, address, contact);
    }

    public void updateNames(String legalName, String tradeName) {
        if (legalName == null || legalName.isBlank()) {
            throw new IllegalArgumentException("Legal name cannot be blank");
        }
        this.legalName = legalName;
        this.tradeName = tradeName;
    }

    public void changeType(CompanyType type) {
        if (type == null) {
            throw new IllegalArgumentException("Company type cannot be null");
        }
        this.type = type;
    }

    public void changeAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        this.address = address;
    }

    public void changeContact(Contact contact) {
        this.contact = contact;
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
