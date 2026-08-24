package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;

@Entity
@Table(
        name = "iam_tenant_roles",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_iam_tenant_role_code",
                    columnNames = {"company_id", "code"}),
            @UniqueConstraint(
                    name = "uk_iam_tenant_role_name",
                    columnNames = {"company_id", "name"})
        })
public class TenantRole extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantRoleType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthorizationStatus status = AuthorizationStatus.ACTIVE;

    protected TenantRole() {}

    private TenantRole(UUID companyId, String code, String name, String description, TenantRoleType type) {
        this.companyId = Objects.requireNonNull(companyId, "Role company cannot be null");
        this.code = Strings.normalizeCode(code, "Role code cannot be blank");
        this.name = Strings.required(name, "Role name cannot be blank");
        this.description = Strings.trimToNull(description);
        this.type = Objects.requireNonNull(type, "Role type cannot be null");
    }

    public static TenantRole builtIn(UUID companyId, String code, String name, String description) {
        return new TenantRole(companyId, code, name, description, TenantRoleType.BUILT_IN);
    }

    public static TenantRole custom(UUID companyId, String name, String description) {
        return new TenantRole(companyId, "CUSTOM_" + UUID.randomUUID(), name, description, TenantRoleType.CUSTOM);
    }

    public void update(String name, String description) {
        if (type == TenantRoleType.BUILT_IN) {
            throw new ConflictException("Built-in roles cannot be edited.");
        }
        if (status == AuthorizationStatus.INACTIVE) {
            throw new ConflictException("Deleted roles cannot be edited.");
        }
        this.name = Strings.required(name, "Role name cannot be blank");
        this.description = Strings.trimToNull(description);
    }

    public void deactivate() {
        if (type == TenantRoleType.BUILT_IN) {
            throw new ConflictException("Built-in roles cannot be deleted.");
        }
        status = AuthorizationStatus.INACTIVE;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TenantRoleType getType() {
        return type;
    }

    public AuthorizationStatus getStatus() {
        return status;
    }
}
