package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.usecase;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.IamPermissions.ROLE_WRITE;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantRoleDeletedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.RolePermission;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;

@Component
public class DeleteTenantRoleUseCase {

    private final TenantRoleRepository roleRepository;
    private final RolePermissionRepository permissionRepository;
    private final TenantRoleAssignmentRepository assignmentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditActorResolver actorResolver;

    public DeleteTenantRoleUseCase(
            TenantRoleRepository roleRepository,
            RolePermissionRepository permissionRepository,
            TenantRoleAssignmentRepository assignmentRepository,
            ApplicationEventPublisher eventPublisher,
            AuditActorResolver actorResolver) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.eventPublisher = eventPublisher;
        this.actorResolver = actorResolver;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + ROLE_WRITE + "')")
    public void execute(UUID roleId) {
        var companyId = TenantContext.requireTenantId();
        TenantRole role = requireRole(companyId, roleId);
        if (assignmentRepository.existsByRoleId(roleId)) {
            throw new ConflictException("Assigned roles cannot be deleted.");
        }
        role.deactivate();
        eventPublisher.publishEvent(new TenantRoleDeletedEvent(
                companyId, roleId, role.getCode(), permissionKeys(roleId), actorResolver.userIdOrNull()));
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
