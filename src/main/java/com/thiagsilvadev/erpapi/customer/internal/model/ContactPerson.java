package com.thiagsilvadev.erpapi.customer.internal.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.thiagsilvadev.erpapi.common.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.model.Contact;

@Entity
@Table(name = "contact_persons")
public class ContactPerson extends AbstractBaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private UUID customerId;

    @Embedded
    private Contact contact;

    protected ContactPerson() {}

    private ContactPerson(String name, UUID customerId, Contact contact) {
        this.name = Customer.required(name, "Contact person name cannot be blank");
        this.customerId = customerId;
        this.contact = contact;
    }

    public static ContactPerson create(String name, UUID customerId, Contact contact) {
        return new ContactPerson(name, customerId, contact);
    }

    public String getName() {
        return name;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public Contact getContact() {
        return contact;
    }
}
