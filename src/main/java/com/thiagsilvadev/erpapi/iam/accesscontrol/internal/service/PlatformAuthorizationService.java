package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.PlatformAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.PlatformRoleActivatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.PlatformRoleAssignedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.PlatformRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.PlatformRoleAssignment;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.PlatformRoleAssignmentRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security.PermissionCatalog;

@Service
public class PlatformAuthorizationService {

    private final PlatformRoleAssignmentRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final PermissionCatalog permissionCatalog;
    private final AuditActorResolver actorResolver;

    public PlatformAuthorizationService(
            PlatformRoleAssignmentRepository repository,
            ApplicationEventPublisher eventPublisher,
            PermissionCatalog permissionCatalog,
            AuditActorResolver actorResolver) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.permissionCatalog = permissionCatalog;
        this.actorResolver = actorResolver;
    }

    @Transactional(readOnly = true)
    public PlatformAuthorizationSnapshot requireActive(UUID userId) {
        PlatformRoleAssignment assignment = repository
                .findByUserId(userId)
                .filter(item -> item.getStatus() == AuthorizationStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedException("An active platform role is required."));
        return snapshot(assignment);
    }

    @Transactional(readOnly = true)
    public PlatformAuthorizationSnapshot validate(UUID assignmentId, UUID userId, long version) {
        PlatformRoleAssignment assignment = repository
                .findById(assignmentId)
                .filter(item -> item.getUserId().equals(userId))
                .filter(item -> item.getStatus() == AuthorizationStatus.ACTIVE)
                .filter(item -> item.getAuthorizationVersion() == version)
                .orElseThrow(() -> new AccessDeniedException("Platform authorization is no longer valid."));
        return snapshot(assignment);
    }

    @Transactional
    public void ensureAdmin(UUID userId) {
        PlatformRoleAssignment assignment = repository.findByUserId(userId).orElse(null);
        if (assignment == null) {
            assignment = repository.save(PlatformRoleAssignment.create(userId, PlatformRole.PLATFORM_ADMIN));
            eventPublisher.publishEvent(new PlatformRoleAssignedEvent(
                    assignment.getId(), userId, PlatformRole.PLATFORM_ADMIN.name(), currentActorUserId()));
            return;
        }
        if (assignment.getStatus() == AuthorizationStatus.INACTIVE) {
            assignment.activate();
            eventPublisher.publishEvent(new PlatformRoleActivatedEvent(
                    assignment.getId(), userId, assignment.getRole().name(), currentActorUserId()));
        }
    }

    private PlatformAuthorizationSnapshot snapshot(PlatformRoleAssignment assignment) {
        return new PlatformAuthorizationSnapshot(
                assignment.getId(),
                assignment.getUserId(),
                assignment.getAuthorizationVersion(),
                assignment.getRole().name(),
                permissionCatalog.platformPermissions());
    }

    private UUID currentActorUserId() {
        return actorResolver.userId().orElse(null);
    }
}
