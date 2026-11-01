package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.IamPermissions.ROLE_WRITE;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantRoleCreatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.CreateTenantRoleRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantRoleResponse;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.mapper.TenantRoleMapper;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.RolePermission;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security.PermissionCatalog;

@Component
public class CreateTenantRoleUseCase {

    private final TenantRoleRepository roleRepository;
    private final RolePermissionRepository permissionRepository;
    private final PermissionCatalog permissionCatalog;
    private final TenantRoleMapper roleMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditActorResolver actorResolver;

    public CreateTenantRoleUseCase(
            TenantRoleRepository roleRepository,
            RolePermissionRepository permissionRepository,
            PermissionCatalog permissionCatalog,
            TenantRoleMapper roleMapper,
            ApplicationEventPublisher eventPublisher,
            AuditActorResolver actorResolver) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionCatalog = permissionCatalog;
        this.roleMapper = roleMapper;
        this.eventPublisher = eventPublisher;
        this.actorResolver = actorResolver;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + ROLE_WRITE + "')")
    public TenantRoleResponse execute(CreateTenantRoleRequest request) {
        var companyId = TenantContext.requireTenantId();
        permissionCatalog.validateGrantable(request.permissions());
        TenantRole role =
                roleRepository.saveAndFlush(TenantRole.custom(companyId, request.name(), request.description()));
        permissionRepository.saveAll(request.permissions().stream()
                .map(key -> RolePermission.create(role.getId(), key))
                .toList());
        eventPublisher.publishEvent(new TenantRoleCreatedEvent(
                companyId, role.getId(), role.getCode(), request.permissions(), actorResolver.userIdOrNull()));
        return roleMapper.toResponse(role);
    }
}
