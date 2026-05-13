package com.thiagsilvadev.erpapi.common.model;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import com.thiagsilvadev.erpapi.common.validation.DomainAssert;

@Embeddable
public class Contact {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, length = 255))
    private Email email;

    @Column(length = 11)
    private String phone;

    protected Contact() {}

    private Contact(String email, String phone) {
        this.email = Email.create(email);
        this.phone = processPhone(phone);
    }

    public static Contact create(String email, String phone) {
        return new Contact(email, phone);
    }

    public Contact changeEmail(String newEmail) {
        return new Contact(newEmail, this.phone);
    }

    public Contact changePhone(String newPhone) {
        return new Contact(this.email.getValue(), newPhone);
    }

    private static String processPhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            return null;
        }

        String normalizedPhone = rawPhone.replaceAll("\\D", "");

        DomainAssert.lengthBetween(normalizedPhone, 10, 11, "Phone with area code must contain 10 or 11 digits");
        return normalizedPhone;
    }

    public String getEmail() {
        return email.getValue();
    }

    public String getPhone() {
        return phone;
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
