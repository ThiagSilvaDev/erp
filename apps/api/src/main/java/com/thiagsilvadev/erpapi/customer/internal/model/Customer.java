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
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_customer_owner_document",
                    columnNames = {"owner_company_id", "document"})
        })
public class Customer extends AbstractBaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 32)
    private String document;

    @Column(name = "owner_company_id", nullable = false)
    private UUID ownerCompanyId;

    @Embedded
    private Contact contact;

    @Embedded
    private Address address;

    protected Customer() {}

    private Customer(String name, String document, UUID ownerCompanyId, Contact contact, Address address) {
        this.name = Strings.required(name, "Customer name");
        this.document = normalizeDocument(document);
        this.ownerCompanyId = requireOwnerCompanyId(ownerCompanyId);
        this.contact = contact;
        this.address = address;
    }

    public static Customer create(String name, String document, UUID ownerCompanyId, Contact contact, Address address) {
        return new Customer(name, document, ownerCompanyId, contact, address);
    }

    public String getName() {
        return name;
    }

    public String getDocument() {
        return document;
    }

    public UUID getOwnerCompanyId() {
        return ownerCompanyId;
    }

    public Contact getContact() {
        return contact;
    }

    public Address getAddress() {
        return address;
    }

    public static String normalizeDocument(String document) {
        return Strings.required(document, "Customer document").replaceAll("\\D", "");
    }

    private static UUID requireOwnerCompanyId(UUID ownerCompanyId) {
        if (ownerCompanyId == null) {
            throw new IllegalArgumentException("Customer owner company cannot be null");
        }
        return ownerCompanyId;
    }
}
