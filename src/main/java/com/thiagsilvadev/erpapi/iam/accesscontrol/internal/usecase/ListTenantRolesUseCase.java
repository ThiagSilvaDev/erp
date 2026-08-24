package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.IamPermissions.ROLE_READ;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantRoleResponse;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.mapper.TenantRoleMapper;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;

/**
 * Lists roles without side effects: built-in role templates are created by company creation and
 * provisioning flows, never by a read. Keeping this query read-only avoids concurrent first-time
 * listings racing on the unique (company, code) constraint and keeps the audit trail free of
 * write events triggered by reads.
 */
@Component
public class ListTenantRolesUseCase {

    private final TenantRoleRepository roleRepository;
    private final TenantRoleMapper roleMapper;

    public ListTenantRolesUseCase(TenantRoleRepository roleRepository, TenantRoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + ROLE_READ + "')")
    public List<TenantRoleResponse> execute() {
        var companyId = TenantContext.requireTenantId();
        return roleRepository.findByCompanyIdAndStatusOrderByName(companyId, AuthorizationStatus.ACTIVE).stream()
                .map(roleMapper::toResponse)
                .toList();
    }
}
