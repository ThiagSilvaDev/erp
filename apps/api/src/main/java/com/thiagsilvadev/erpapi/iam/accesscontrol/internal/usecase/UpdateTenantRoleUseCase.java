package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.IamPermissions.ROLE_WRITE;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantRoleUpdatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.TenantRoleResponse;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto.UpdateTenantRoleRequest;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.mapper.TenantRoleMapper;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.RolePermission;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantMembershipRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security.PermissionCatalog;

@Component
public class UpdateTenantRoleUseCase {

    private final TenantRoleRepository roleRepository;
    private final RolePermissionRepository permissionRepository;
    private final TenantRoleAssignmentRepository assignmentRepository;
    private final TenantMembershipRepository membershipRepository;
    private final PermissionCatalog permissionCatalog;
    private final TenantRoleMapper roleMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditActorResolver actorResolver;

    public UpdateTenantRoleUseCase(
            TenantRoleRepository roleRepository,
            RolePermissionRepository permissionRepository,
            TenantRoleAssignmentRepository assignmentRepository,
            TenantMembershipRepository membershipRepository,
            PermissionCatalog permissionCatalog,
            TenantRoleMapper roleMapper,
            ApplicationEventPublisher eventPublisher,
            AuditActorResolver actorResolver) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.membershipRepository = membershipRepository;
        this.permissionCatalog = permissionCatalog;
        this.roleMapper = roleMapper;
        this.eventPublisher = eventPublisher;
        this.actorResolver = actorResolver;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + ROLE_WRITE + "')")
    public TenantRoleResponse execute(UUID roleId, UpdateTenantRoleRequest request) {
        var companyId = TenantContext.requireTenantId();
        TenantRole role = requireRole(companyId, roleId);
        role.update(request.name(), request.description());
        permissionCatalog.validateGrantable(request.permissions());
        Set<String> before = permissionKeys(roleId);
        permissionRepository.deleteByRoleId(roleId);
        permissionRepository.flush();
        permissionRepository.saveAll(request.permissions().stream()
                .map(key -> RolePermission.create(roleId, key))
                .toList());
        assignmentRepository.findMembershipIdsByRoleId(roleId).stream()
                .map(membershipRepository::findById)
                .flatMap(Optional::stream)
                .forEach(membership -> membership.touchAuthorization());
        eventPublisher.publishEvent(new TenantRoleUpdatedEvent(
                companyId, roleId, role.getCode(), before, request.permissions(), actorResolver.userIdOrNull()));
        return roleMapper.toResponse(role);
    }

    private TenantRole requireRole(UUID companyId, UUID roleId) {
        return roleRepository
                .findByIdAndCompanyId(roleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(TenantRole.class, roleId));
    }

    private Set<String> permissionKeys(UUID roleId) {
        return permissionRepository.findByRoleId(roleId).stream()
                .map(RolePermission::getPermissionKey)
                .collect(Collectors.toUnmodifiableSet());
    }
}
