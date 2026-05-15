package com.thiagsilvadev.erpapi.customer.internal.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.common.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.model.Contact;
import com.thiagsilvadev.erpapi.common.model.address.Address;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = {@UniqueConstraint(name = "uk_customer_document", columnNames = "document")})
public class Customer extends AbstractBaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 32)
    private String document;

    @Column(nullable = false)
    private UUID companyId;

    @Embedded
    private Contact contact;

    @Embedded
    private Address address;

    protected Customer() {}

    private Customer(String name, String document, UUID companyId, Contact contact, Address address) {
        this.name = required(name, "Customer name cannot be blank");
        this.document = normalizeDocument(document);
        this.companyId = requireCompanyId(companyId);
        this.contact = contact;
        this.address = address;
    }

    public static Customer create(String name, String document, UUID companyId, Contact contact, Address address) {
        return new Customer(name, document, companyId, contact, address);
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
        return required(document, "Customer document cannot be blank").replaceAll("\\D", "");
    }

    private static UUID requireCompanyId(UUID companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("Customer company cannot be null");
        }
        return companyId;
    }

    static String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
