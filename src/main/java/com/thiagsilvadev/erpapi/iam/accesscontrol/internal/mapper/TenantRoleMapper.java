package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.mapper;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantRoleResponse;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.RolePermission;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;

@Component
public class TenantRoleMapper {

    private final RolePermissionRepository permissionRepository;

    public TenantRoleMapper(RolePermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public TenantRoleResponse toResponse(TenantRole role) {
        return new TenantRoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getType(),
                role.getStatus(),
                permissionKeys(role.getId()));
    }

    private Set<String> permissionKeys(UUID roleId) {
        return permissionRepository.findByRoleId(roleId).stream()
                .map(RolePermission::getPermissionKey)
                .collect(Collectors.toUnmodifiableSet());
    }
}
