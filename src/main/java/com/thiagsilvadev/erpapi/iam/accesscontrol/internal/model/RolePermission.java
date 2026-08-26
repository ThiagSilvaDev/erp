package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Entity
@Table(
        name = "iam_role_permissions",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_iam_role_permission",
                    columnNames = {"role_id", "permission_key"})
        })
public class RolePermission extends AbstractBaseEntity {

    @Column(name = "role_id", nullable = false, updatable = false)
    private UUID roleId;

    @Column(name = "permission_key", nullable = false, updatable = false, length = 100)
    private String permissionKey;

    protected RolePermission() {}

    private RolePermission(UUID roleId, String permissionKey) {
        this.roleId = Objects.requireNonNull(roleId, "Permission role cannot be null");
        this.permissionKey = Strings.required(permissionKey, "Permission key");
    }

    public static RolePermission create(UUID roleId, String permissionKey) {
        return new RolePermission(roleId, permissionKey);
    }

    public UUID getRoleId() {
        return roleId;
    }

    public String getPermissionKey() {
        return permissionKey;
    }
}
