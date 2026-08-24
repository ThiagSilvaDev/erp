package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;

@Entity
@Table(
        name = "iam_tenant_role_assignments",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_iam_membership_role",
                    columnNames = {"membership_id", "role_id", "scope_type", "scope_id"})
        },
        indexes = {
            @Index(
                    name = "idx_iam_tenant_role_assignments_membership_scope",
                    columnList = "membership_id, scope_type, scope_id"),
            @Index(name = "idx_iam_tenant_role_assignments_role_scope", columnList = "role_id, scope_type, scope_id")
        })
public class TenantRoleAssignment extends AbstractBaseEntity {

    @Column(name = "membership_id", nullable = false, updatable = false)
    private UUID membershipId;

    @Column(name = "role_id", nullable = false, updatable = false)
    private UUID roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, updatable = false, length = 20)
    private AuthorizationScopeType scopeType;

    @Column(name = "scope_id", updatable = false)
    private UUID scopeId;

    protected TenantRoleAssignment() {}

    private TenantRoleAssignment(UUID membershipId, UUID roleId, AuthorizationScopeType scopeType, UUID scopeId) {
        this.membershipId = Objects.requireNonNull(membershipId, "Assignment membership cannot be null");
        this.roleId = Objects.requireNonNull(roleId, "Assignment role cannot be null");
        this.scopeType = Objects.requireNonNull(scopeType, "Assignment scope type cannot be null");
        this.scopeId = Objects.requireNonNull(scopeId, "Assignment scope id cannot be null");
    }

    public static TenantRoleAssignment tenant(UUID membershipId, UUID roleId, UUID tenantId) {
        return new TenantRoleAssignment(membershipId, roleId, AuthorizationScopeType.TENANT, tenantId);
    }

    public static TenantRoleAssignment scoped(
            UUID membershipId, UUID roleId, AuthorizationScopeType scopeType, UUID scopeId) {
        return new TenantRoleAssignment(membershipId, roleId, scopeType, scopeId);
    }

    public UUID getMembershipId() {
        return membershipId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public AuthorizationScopeType getScopeType() {
        return scopeType;
    }

    public UUID getScopeId() {
        return scopeId;
    }
}
