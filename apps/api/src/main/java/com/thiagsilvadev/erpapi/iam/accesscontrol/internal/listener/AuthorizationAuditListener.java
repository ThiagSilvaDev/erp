package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.listener;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.PlatformRoleActivatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.PlatformRoleAssignedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipActivatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipDeactivatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipProvisionedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipRolesReplacedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantRoleCreatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantRoleDeletedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantRoleUpdatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationAction;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationActorType;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationAudit;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationTargetType;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Records authorization changes as {@link AuthorizationAudit} entries.
 *
 * <p>Entries are written in the {@link TransactionPhase#BEFORE_COMMIT} phase so that the audit row
 * commits atomically with the business change it records: if the audit write fails, the business
 * transaction rolls back instead of committing an unaudited security-relevant change. A side
 * effect of using {@code BEFORE_COMMIT} is that these events are not registered in the Spring
 * Modulith event publication registry (which only tracks {@code AFTER_COMMIT} listeners), so they
 * are persisted exactly once, in the audit table.</p>
 *
 * <p>Payloads follow a consistent contract: {@code before_value} and {@code after_value} always
 * hold the full pre- and post-state as JSON, or {@code null} when no state existed on that side
 * (creation and deletion).</p>
 */
@Component
public class AuthorizationAuditListener {

    private final AuthorizationAuditWriter writer;
    private final ObjectMapper objectMapper;

    public AuthorizationAuditListener(AuthorizationAuditWriter writer, ObjectMapper objectMapper) {
        this.writer = writer;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void on(TenantMembershipProvisionedEvent event) {
        record(
                event.companyId(),
                AuthorizationTargetType.MEMBERSHIP,
                event.membershipId(),
                AuthorizationAction.MEMBERSHIP_PROVISIONED,
                null,
                json(Map.of("userId", event.userId(), "roleIds", event.roleIds())),
                event.actorUserId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void on(TenantMembershipActivatedEvent event) {
        record(
                event.companyId(),
                AuthorizationTargetType.MEMBERSHIP,
                event.membershipId(),
                AuthorizationAction.MEMBERSHIP_ACTIVATED,
                null,
                json(Map.of("userId", event.userId(), "roleIds", event.roleIds())),
                event.actorUserId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void on(TenantMembershipDeactivatedEvent event) {
        record(
                event.companyId(),
                AuthorizationTargetType.MEMBERSHIP,
                event.membershipId(),
                AuthorizationAction.MEMBERSHIP_DEACTIVATED,
                json(Map.of("userId", event.userId(), "roleIds", event.roleIds())),
                null,
                event.actorUserId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void on(TenantMembershipRolesReplacedEvent event) {
        record(
                event.companyId(),
                AuthorizationTargetType.MEMBERSHIP,
                event.membershipId(),
                AuthorizationAction.MEMBERSHIP_ROLES_REPLACED,
                json("roleIds", event.beforeRoleIds()),
                json("roleIds", event.afterRoleIds()),
                event.actorUserId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void on(TenantRoleCreatedEvent event) {
        record(
                event.companyId(),
                AuthorizationTargetType.ROLE,
                event.roleId(),
                AuthorizationAction.ROLE_CREATED,
                null,
                json(Map.of("code", event.roleCode(), "permissions", event.permissions())),
                event.actorUserId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void on(TenantRoleUpdatedEvent event) {
        record(
                event.companyId(),
                AuthorizationTargetType.ROLE,
                event.roleId(),
                AuthorizationAction.ROLE_UPDATED,
                json(Map.of("code", event.roleCode(), "permissions", event.beforePermissions())),
                json(Map.of("code", event.roleCode(), "permissions", event.afterPermissions())),
                event.actorUserId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void on(TenantRoleDeletedEvent event) {
        record(
                event.companyId(),
                AuthorizationTargetType.ROLE,
                event.roleId(),
                AuthorizationAction.ROLE_DELETED,
                json(Map.of("code", event.roleCode(), "permissions", event.permissions())),
                null,
                event.actorUserId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void on(PlatformRoleAssignedEvent event) {
        record(
                null,
                AuthorizationTargetType.PLATFORM_ASSIGNMENT,
                event.assignmentId(),
                AuthorizationAction.PLATFORM_ROLE_ASSIGNED,
                null,
                json(Map.of("userId", event.userId(), "role", event.role())),
                event.actorUserId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void on(PlatformRoleActivatedEvent event) {
        record(
                null,
                AuthorizationTargetType.PLATFORM_ASSIGNMENT,
                event.assignmentId(),
                AuthorizationAction.PLATFORM_ROLE_ACTIVATED,
                null,
                json(Map.of("userId", event.userId(), "role", event.role())),
                event.actorUserId());
    }

    private void record(
            UUID companyId,
            AuthorizationTargetType targetType,
            UUID targetId,
            AuthorizationAction action,
            String beforeValue,
            String afterValue,
            UUID actorUserId) {
        AuthorizationActorType actorType =
                actorUserId == null ? AuthorizationActorType.UNKNOWN : AuthorizationActorType.USER;
        writer.write(AuthorizationAudit.create(
                companyId, actorUserId, actorType, targetType, targetId, action, beforeValue, afterValue));
    }

    private String json(String key, Object value) {
        return json(Map.of(key, value));
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize audit payload", exception);
        }
    }
}
