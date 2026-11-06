package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.RolePermission;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    List<RolePermission> findByRoleId(UUID roleId);

    List<RolePermission> findByRoleIdIn(Collection<UUID> roleIds);

    void deleteByRoleId(UUID roleId);
}
