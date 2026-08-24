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
        name = "iam_tenant_memberships",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_iam_tenant_membership",
                    columnNames = {"user_id", "company_id"})
        })
public class TenantMembership extends AbstractBaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthorizationStatus status = AuthorizationStatus.ACTIVE;

    @Column(name = "authorization_version", nullable = false)
    private long authorizationVersion;

    protected TenantMembership() {}

    private TenantMembership(UUID userId, UUID companyId) {
        this.userId = Objects.requireNonNull(userId, "Membership user cannot be null");
        this.companyId = Objects.requireNonNull(companyId, "Membership company cannot be null");
    }

    public static TenantMembership create(UUID userId, UUID companyId) {
        return new TenantMembership(userId, companyId);
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

    public UUID getCompanyId() {
        return companyId;
    }

    public AuthorizationStatus getStatus() {
        return status;
    }

    public long getAuthorizationVersion() {
        return authorizationVersion;
    }
}
