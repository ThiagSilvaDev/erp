package com.thiagsilvadev.erpapi.company.internal.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;

@Entity
@Table(
        name = "company_contacts",
        indexes = {
            @Index(name = "idx_company_contact_company", columnList = "company_id"),
            @Index(name = "idx_company_contact_primary", columnList = "company_id, primary_contact")
        })
public class CompanyContact extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private String name;

    private String role;

    @Embedded
    private Contact contact;

    @Column(name = "primary_contact", nullable = false)
    private boolean primaryContact;

    protected CompanyContact() {}

    private CompanyContact(UUID companyId, String name, String role, Contact contact, boolean primaryContact) {
        this.companyId = requireCompanyId(companyId);
        this.name = required(name, "Company contact name cannot be blank");
        this.role = trimToNull(role);
        this.contact = contact;
        this.primaryContact = primaryContact;
    }

    public static CompanyContact create(
            UUID companyId, String name, String role, Contact contact, boolean primaryContact) {
        return new CompanyContact(companyId, name, role, contact, primaryContact);
    }

    public void markPrimary() {
        this.primaryContact = true;
    }

    public void unmarkPrimary() {
        this.primaryContact = false;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public Contact getContact() {
        return contact;
    }

    public boolean isPrimaryContact() {
        return primaryContact;
    }

    private static UUID requireCompanyId(UUID companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("Company contact company cannot be null");
        }
        return companyId;
    }

    private static String required(String value, String message) {
        String trimmedValue = trimToNull(value);
        if (trimmedValue == null) {
            throw new IllegalArgumentException(message);
        }
        return trimmedValue;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
