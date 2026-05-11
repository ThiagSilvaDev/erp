package com.thiagsilvadev.erpapi.common.model;

import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.thiagsilvadev.erpapi.common.validation.DomainAssert;

@Embeddable
public class Contact {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    @Column(length = 255, nullable = false)
    private String email;

    @Column(length = 11)
    private String phone;

    protected Contact() {}

    private Contact(String email, String phone) {
        this.email = processEmail(email);
        this.phone = processPhone(phone);
    }

    public static Contact create(String email, String phone) {
        return new Contact(email, phone);
    }

    public Contact changeEmail(String newEmail) {
        return new Contact(newEmail, this.phone);
    }

    public Contact changePhone(String newPhone) {
        return new Contact(this.email, newPhone);
    }

    private static String processEmail(String rawEmail) {
        String normalizedEmail = rawEmail == null ? null : rawEmail.trim().toLowerCase();

        DomainAssert.notBlank(normalizedEmail, "Email is required");
        DomainAssert.maxLength(normalizedEmail, 255, "Email must contain at most 255 characters");
        DomainAssert.matchesPattern(normalizedEmail, EMAIL_PATTERN, "Invalid email format");
        return normalizedEmail;
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
        return email;
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
