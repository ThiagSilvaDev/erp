package com.thiagsilvadev.erpapi.user.internal.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.user.api.UserMembershipStatus;
import com.thiagsilvadev.erpapi.user.api.UserRole;

@Entity
@Table(
        name = "user_company_memberships",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_user_company_membership",
                    columnNames = {"user_id", "company_id"})
        })
public class UserMembership extends AbstractBaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserMembershipStatus status = UserMembershipStatus.ACTIVE;

    protected UserMembership() {}

    private UserMembership(UUID userId, UUID companyId, UserRole role) {
        this.userId = requireNonNull(userId, "Membership user cannot be null");
        this.companyId = requireNonNull(companyId, "Membership company cannot be null");
        this.role = requireNonNull(role, "Membership role cannot be null");
    }

    public static UserMembership create(UUID userId, UUID companyId, UserRole role) {
        return new UserMembership(userId, companyId, role);
    }

    public void activate() {
        this.status = UserMembershipStatus.ACTIVE;
    }

    public void inactivate() {
        this.status = UserMembershipStatus.INACTIVE;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UserRole getRole() {
        return role;
    }

    public UserMembershipStatus getStatus() {
        return status;
    }

    private static <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
