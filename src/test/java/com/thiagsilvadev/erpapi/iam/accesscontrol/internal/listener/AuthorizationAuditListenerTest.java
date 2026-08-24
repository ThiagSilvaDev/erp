package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

import tools.jackson.databind.json.JsonMapper;

class AuthorizationAuditListenerTest {

    private final AuthorizationAuditWriter writer = mock(AuthorizationAuditWriter.class);
    private final AuthorizationAuditListener listener =
            new AuthorizationAuditListener(writer, JsonMapper.builder().build());

    @Test
    void shouldRecordProvisionedMembershipForUserActor() {
        UUID companyId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();

        listener.on(new TenantMembershipProvisionedEvent(companyId, membershipId, userId, Set.of(roleId), actorUserId));

        AuthorizationAudit audit = savedAudit();
        assertThat(audit.getCompanyId()).isEqualTo(companyId);
        assertThat(audit.getTargetType()).isEqualTo(AuthorizationTargetType.MEMBERSHIP);
        assertThat(audit.getTargetId()).isEqualTo(membershipId);
        assertThat(audit.getAction()).isEqualTo(AuthorizationAction.MEMBERSHIP_PROVISIONED);
        assertThat(audit.getActorType()).isEqualTo(AuthorizationActorType.USER);
        assertThat(audit.getActorUserId()).isEqualTo(actorUserId);
        assertThat(audit.getBeforeValue()).isNull();
        assertThat(audit.getAfterValue())
                .contains("userId")
                .contains(userId.toString())
                .contains("roleIds")
                .contains(roleId.toString());
    }

    @Test
    void shouldRecordUnknownActorWhenActorIsMissing() {
        UUID companyId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();

        listener.on(new TenantMembershipActivatedEvent(
                companyId, membershipId, UUID.randomUUID(), Set.of(UUID.randomUUID()), null));

        AuthorizationAudit audit = savedAudit();
        assertThat(audit.getAction()).isEqualTo(AuthorizationAction.MEMBERSHIP_ACTIVATED);
        assertThat(audit.getActorType()).isEqualTo(AuthorizationActorType.UNKNOWN);
        assertThat(audit.getActorUserId()).isNull();
    }

    @Test
    void shouldRecordActivatedMembershipWithUserAndRoles() {
        UUID companyId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        listener.on(new TenantMembershipActivatedEvent(companyId, membershipId, userId, Set.of(roleId), null));

        AuthorizationAudit audit = savedAudit();
        assertThat(audit.getAction()).isEqualTo(AuthorizationAction.MEMBERSHIP_ACTIVATED);
        assertThat(audit.getBeforeValue()).isNull();
        assertThat(audit.getAfterValue()).contains(userId.toString()).contains(roleId.toString());
    }

    @Test
    void shouldRecordDeactivatedMembershipWithUserAndRoles() {
        UUID companyId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        listener.on(new TenantMembershipDeactivatedEvent(
                companyId, membershipId, userId, Set.of(roleId), UUID.randomUUID()));

        AuthorizationAudit audit = savedAudit();
        assertThat(audit.getAction()).isEqualTo(AuthorizationAction.MEMBERSHIP_DEACTIVATED);
        assertThat(audit.getTargetId()).isEqualTo(membershipId);
        assertThat(audit.getBeforeValue()).contains(userId.toString()).contains(roleId.toString());
        assertThat(audit.getAfterValue()).isNull();
    }

    @Test
    void shouldRecordReplacedRolesWithFullBeforeAndAfterStates() {
        UUID companyId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        UUID keptRoleId = UUID.randomUUID();
        UUID removedRoleId = UUID.randomUUID();
        UUID addedRoleId = UUID.randomUUID();

        listener.on(new TenantMembershipRolesReplacedEvent(
                companyId, membershipId, Set.of(keptRoleId, removedRoleId), Set.of(keptRoleId, addedRoleId), null));

        AuthorizationAudit audit = savedAudit();
        assertThat(audit.getAction()).isEqualTo(AuthorizationAction.MEMBERSHIP_ROLES_REPLACED);
        assertThat(audit.getBeforeValue())
                .contains(keptRoleId.toString())
                .contains(removedRoleId.toString())
                .doesNotContain(addedRoleId.toString());
        assertThat(audit.getAfterValue())
                .contains(keptRoleId.toString())
                .contains(addedRoleId.toString())
                .doesNotContain(removedRoleId.toString());
    }

    @Test
    void shouldRecordRoleCreationWithCodeAndPermissions() {
        UUID companyId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        listener.on(new TenantRoleCreatedEvent(companyId, roleId, "CUSTOM_1", Set.of("catalog:read"), null));

        AuthorizationAudit audit = savedAudit();
        assertThat(audit.getTargetType()).isEqualTo(AuthorizationTargetType.ROLE);
        assertThat(audit.getAction()).isEqualTo(AuthorizationAction.ROLE_CREATED);
        assertThat(audit.getBeforeValue()).isNull();
        assertThat(audit.getAfterValue()).contains("CUSTOM_1").contains("catalog:read");
    }

    @Test
    void shouldRecordRoleUpdateWithFullBeforeAndAfterStates() {
        UUID companyId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        listener.on(new TenantRoleUpdatedEvent(
                companyId,
                roleId,
                "CUSTOM_1",
                Set.of("catalog:read", "catalog:list"),
                Set.of("catalog:write", "catalog:list"),
                null));

        AuthorizationAudit audit = savedAudit();
        assertThat(audit.getAction()).isEqualTo(AuthorizationAction.ROLE_UPDATED);
        assertThat(audit.getBeforeValue())
                .contains("CUSTOM_1")
                .contains("catalog:read")
                .contains("catalog:list")
                .doesNotContain("catalog:write");
        assertThat(audit.getAfterValue())
                .contains("CUSTOM_1")
                .contains("catalog:write")
                .contains("catalog:list")
                .doesNotContain("catalog:read");
    }

    @Test
    void shouldRecordRoleDeletionWithFullBeforeState() {
        UUID companyId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        listener.on(new TenantRoleDeletedEvent(companyId, roleId, "CUSTOM_1", Set.of("catalog:write"), null));

        AuthorizationAudit audit = savedAudit();
        assertThat(audit.getAction()).isEqualTo(AuthorizationAction.ROLE_DELETED);
        assertThat(audit.getAfterValue()).isNull();
        assertThat(audit.getBeforeValue()).contains("CUSTOM_1").contains("catalog:write");
    }

    @Test
    void shouldRecordPlatformAssignmentWithoutCompany() {
        UUID assignmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        listener.on(new PlatformRoleAssignedEvent(assignmentId, userId, "PLATFORM_ADMIN", null));

        AuthorizationAudit audit = savedAudit();
        assertThat(audit.getCompanyId()).isNull();
        assertThat(audit.getTargetType()).isEqualTo(AuthorizationTargetType.PLATFORM_ASSIGNMENT);
        assertThat(audit.getTargetId()).isEqualTo(assignmentId);
        assertThat(audit.getAction()).isEqualTo(AuthorizationAction.PLATFORM_ROLE_ASSIGNED);
        assertThat(audit.getAfterValue()).contains("PLATFORM_ADMIN").contains(userId.toString());
    }

    @Test
    void shouldRecordPlatformReactivation() {
        UUID assignmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        listener.on(new PlatformRoleActivatedEvent(assignmentId, userId, "PLATFORM_ADMIN", null));

        AuthorizationAudit audit = savedAudit();
        assertThat(audit.getCompanyId()).isNull();
        assertThat(audit.getTargetType()).isEqualTo(AuthorizationTargetType.PLATFORM_ASSIGNMENT);
        assertThat(audit.getTargetId()).isEqualTo(assignmentId);
        assertThat(audit.getAction()).isEqualTo(AuthorizationAction.PLATFORM_ROLE_ACTIVATED);
        assertThat(audit.getAfterValue()).contains("PLATFORM_ADMIN").contains(userId.toString());
    }

    private AuthorizationAudit savedAudit() {
        ArgumentCaptor<AuthorizationAudit> captor = ArgumentCaptor.forClass(AuthorizationAudit.class);
        verify(writer, atLeastOnce()).write(captor.capture());
        List<AuthorizationAudit> values = captor.getAllValues();
        return values.get(values.size() - 1);
    }
}
