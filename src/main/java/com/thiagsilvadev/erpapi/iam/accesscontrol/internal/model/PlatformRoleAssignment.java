package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;

@Entity
@Table(
        name = "iam_platform_role_assignments",
        uniqueConstraints = {@UniqueConstraint(name = "uk_iam_platform_user", columnNames = "user_id")})
public class PlatformRoleAssignment extends AbstractBaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlatformRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthorizationStatus status = AuthorizationStatus.ACTIVE;

    @Column(name = "authorization_version", nullable = false)
    private long authorizationVersion;

    protected PlatformRoleAssignment() {}

    private PlatformRoleAssignment(UUID userId, PlatformRole role) {
        this.userId = Objects.requireNonNull(userId, "Platform assignment user is required");
        this.role = Objects.requireNonNull(role, "Platform assignment role is required");
    }

    public static PlatformRoleAssignment create(UUID userId, PlatformRole role) {
        return new PlatformRoleAssignment(userId, role);
    }

    public void activate() {
        status = AuthorizationStatus.ACTIVE;
        touchAuthorization();
    }

    public void deactivate() {
        status = AuthorizationStatus.INACTIVE;
        touchAuthorization();
    }

    public void touchAuthorization() {
        authorizationVersion++;
    }

    public UUID getUserId() {
        return userId;
    }

    public PlatformRole getRole() {
        return role;
    }

    public AuthorizationStatus getStatus() {
        return status;
    }

    public long getAuthorizationVersion() {
        return authorizationVersion;
    }
}
