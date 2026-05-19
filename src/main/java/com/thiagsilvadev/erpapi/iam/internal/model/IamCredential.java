package com.thiagsilvadev.erpapi.iam.internal.model;

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
import com.thiagsilvadev.erpapi.common.domain.model.Email;

@Entity
@Table(
        name = "iam_credentials",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_iam_credential_user", columnNames = "user_id"),
            @UniqueConstraint(name = "uk_iam_credential_email", columnNames = "email")
        })
public class IamCredential extends AbstractBaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "email", nullable = false, updatable = false, length = 255))
    private Email email;

    @Column(nullable = false, length = 120)
    private String passwordHash;

    @Column(nullable = false, length = 40)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CredentialStatus status = CredentialStatus.ACTIVE;

    protected IamCredential() {}

    private IamCredential(UUID userId, String email, String passwordHash, String role) {
        this.userId = requireUserId(userId);
        this.email = Email.create(email);
        this.passwordHash = requireText(passwordHash, "Password hash cannot be blank");
        this.role = requireRole(role);
    }

    public static IamCredential create(UUID userId, String email, String passwordHash, String role) {
        return new IamCredential(userId, email, passwordHash, role);
    }

    public void activate() {
        this.status = CredentialStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = CredentialStatus.INACTIVE;
    }

    public boolean isActive() {
        return status == CredentialStatus.ACTIVE;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email.getValue();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public CredentialStatus getStatus() {
        return status;
    }

    private static UUID requireUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Credential user cannot be null");
        }
        return userId;
    }

    private static String requireRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        return role.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
