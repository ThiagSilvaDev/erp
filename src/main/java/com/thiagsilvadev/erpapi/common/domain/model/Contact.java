package com.thiagsilvadev.erpapi.common.domain.model;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class Contact {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, length = 255))
    private Email email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone", length = 11))
    private Phone phone;

    protected Contact() {}

    private Contact(String email, String phone) {
        this.email = Email.create(email);
        this.phone = Phone.optional(phone);
    }

    public static Contact create(String email, String phone) {
        return new Contact(email, phone);
    }

    public Contact changeEmail(String newEmail) {
        return new Contact(newEmail, getPhone());
    }

    public Contact changePhone(String newPhone) {
        return new Contact(this.email.getValue(), newPhone);
    }

    public String getEmail() {
        return email.getValue();
    }

    public String getPhone() {
        return phone == null ? null : phone.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact contact)) return false;
        return Objects.equals(email, contact.email) && Objects.equals(phone, contact.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, phone);
    }
}
