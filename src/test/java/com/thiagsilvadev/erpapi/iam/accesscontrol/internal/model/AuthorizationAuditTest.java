package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.exception.UserActorIdRequiredException;

class AuthorizationAuditTest {

    @Test
    void shouldRequireUserActorId() {
        assertThatThrownBy(() -> AuthorizationAudit.create(
                        UUID.randomUUID(),
                        null,
                        AuthorizationActorType.USER,
                        AuthorizationTargetType.ROLE,
                        UUID.randomUUID(),
                        AuthorizationAction.ROLE_CREATED,
                        null,
                        null))
                .isInstanceOf(UserActorIdRequiredException.class)
                .hasMessageContaining("actor");
    }

    @Test
    void shouldAllowSystemActorWithoutUserId() {
        AuthorizationAudit audit = AuthorizationAudit.create(
                UUID.randomUUID(),
                null,
                AuthorizationActorType.SYSTEM,
                AuthorizationTargetType.ROLE,
                UUID.randomUUID(),
                AuthorizationAction.ROLE_CREATED,
                null,
                null);

        assertThat(audit.getActorType()).isEqualTo(AuthorizationActorType.SYSTEM);
        assertThat(audit.getActorUserId()).isNull();
    }

    @Test
    void shouldAllowUnknownActorWithoutUserId() {
        AuthorizationAudit audit = AuthorizationAudit.create(
                UUID.randomUUID(),
                null,
                AuthorizationActorType.UNKNOWN,
                AuthorizationTargetType.ROLE,
                UUID.randomUUID(),
                AuthorizationAction.ROLE_CREATED,
                null,
                null);

        assertThat(audit.getActorType()).isEqualTo(AuthorizationActorType.UNKNOWN);
        assertThat(audit.getActorUserId()).isNull();
    }

    @Test
    void shouldRequireTargetId() {
        assertThatThrownBy(() -> AuthorizationAudit.create(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        AuthorizationActorType.USER,
                        AuthorizationTargetType.ROLE,
                        null,
                        AuthorizationAction.ROLE_CREATED,
                        null,
                        null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void shouldAllowNullCompanyForPlatformScope() {
        AuthorizationAudit audit = AuthorizationAudit.create(
                null,
                UUID.randomUUID(),
                AuthorizationActorType.USER,
                AuthorizationTargetType.PLATFORM_ASSIGNMENT,
                UUID.randomUUID(),
                AuthorizationAction.PLATFORM_ROLE_ASSIGNED,
                null,
                null);

        assertThat(audit.getCompanyId()).isNull();
    }

    @Test
    void shouldTrimBlankPayloadValues() {
        UUID companyId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        AuthorizationAudit audit = AuthorizationAudit.create(
                companyId,
                UUID.randomUUID(),
                AuthorizationActorType.USER,
                AuthorizationTargetType.MEMBERSHIP,
                targetId,
                AuthorizationAction.MEMBERSHIP_ACTIVATED,
                " ",
                null);

        assertThat(audit.getBeforeValue()).isNull();
        assertThat(audit.getCompanyId()).isEqualTo(companyId);
        assertThat(audit.getTargetId()).isEqualTo(targetId);
    }
}
