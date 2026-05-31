package com.thiagsilvadev.erpapi.customer.internal.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;

@Entity
@Table(
        name = "suppliers",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_supplier_company_document",
                    columnNames = {"company_id", "document"})
        })
public class Supplier extends AbstractBaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 32)
    private String document;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Embedded
    private Contact contact;

    @Embedded
    private Address address;

    protected Supplier() {}

    private Supplier(String name, String document, UUID companyId, Contact contact, Address address) {
        this.name = Customer.required(name, "Supplier name cannot be blank");
        this.document = normalizeDocument(document);
        this.companyId = companyId;
        this.contact = contact;
        this.address = address;
    }

    public static Supplier create(String name, String document, UUID companyId, Contact contact, Address address) {
        return new Supplier(name, document, companyId, contact, address);
    }

    public String getName() {
        return name;
    }

    public String getDocument() {
        return document;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public Contact getContact() {
        return contact;
    }

    public Address getAddress() {
        return address;
    }

    public static String normalizeDocument(String document) {
        return Customer.required(document, "Supplier document cannot be blank").replaceAll("\\D", "");
    }
}
