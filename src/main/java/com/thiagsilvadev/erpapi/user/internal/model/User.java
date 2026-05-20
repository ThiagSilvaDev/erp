package com.thiagsilvadev.erpapi.user.internal.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.user.api.UserRole;
import com.thiagsilvadev.erpapi.user.api.UserStatus;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {@UniqueConstraint(name = "uk_user_email", columnNames = "email")})
public class User extends AbstractBaseEntity {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Embedded
    @AttributeOverride(name = "email.value", column = @Column(name = "email", nullable = false, length = 255))
    @AttributeOverride(name = "phone.value", column = @Column(name = "phone", length = 11))
    private Contact contact;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private UUID companyId;

    protected User() {}

    private User(
            String firstName, String lastName, Contact contact, LocalDate birthDate, UserRole role, UUID companyId) {
        this.firstName = normalizeRequired(firstName, "User first name cannot be blank");
        this.lastName = normalizeRequired(lastName, "User last name cannot be blank");
        this.contact = requireContact(contact);
        this.birthDate = birthDate;
        this.role = requireRole(role);
        this.companyId = requireCompanyId(companyId);
    }

    public static User create(
            String firstName, String lastName, Contact contact, LocalDate birthDate, UserRole role, UUID companyId) {
        return new User(firstName, lastName, contact, birthDate, role, companyId);
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void inactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Contact getContact() {
        return contact;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    private static Contact requireContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("User contact cannot be null");
        }
        return contact;
    }

    private static UserRole requireRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
        return role;
    }

    private static UUID requireCompanyId(UUID companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("User company cannot be null");
        }
        return companyId;
    }

    private static String normalizeRequired(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
