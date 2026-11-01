package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.INACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.InvalidAttributeException;

class PlatformRoleAssignmentTest {

    @Test
    void shouldRequireUser() {
        assertThatThrownBy(() -> PlatformRoleAssignment.create(null, PlatformRole.PLATFORM_ADMIN))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessageContaining("user");
    }

    @Test
    void shouldRequireRole() {
        assertThatThrownBy(() -> PlatformRoleAssignment.create(UUID.randomUUID(), null))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessageContaining("role");
    }

    @Test
    void shouldStartActiveWithZeroAuthorizationVersion() {
        PlatformRoleAssignment assignment =
                PlatformRoleAssignment.create(UUID.randomUUID(), PlatformRole.PLATFORM_ADMIN);

        assertThat(assignment.getStatus()).isEqualTo(ACTIVE);
        assertThat(assignment.getAuthorizationVersion()).isZero();
    }

    @Test
    void shouldBumpAuthorizationVersionOnActivation() {
        PlatformRoleAssignment assignment =
                PlatformRoleAssignment.create(UUID.randomUUID(), PlatformRole.PLATFORM_ADMIN);
        assignment.deactivate();

        assignment.activate();

        assertThat(assignment.getStatus()).isEqualTo(ACTIVE);
        assertThat(assignment.getAuthorizationVersion()).isEqualTo(2L);
    }

    @Test
    void shouldBumpAuthorizationVersionOnDeactivation() {
        PlatformRoleAssignment assignment =
                PlatformRoleAssignment.create(UUID.randomUUID(), PlatformRole.PLATFORM_ADMIN);

        assignment.deactivate();

        assertThat(assignment.getStatus()).isEqualTo(INACTIVE);
        assertThat(assignment.getAuthorizationVersion()).isEqualTo(1L);
    }

    @Test
    void shouldBumpAuthorizationVersionOnTouch() {
        PlatformRoleAssignment assignment =
                PlatformRoleAssignment.create(UUID.randomUUID(), PlatformRole.PLATFORM_ADMIN);

        assignment.touchAuthorization();
        assignment.touchAuthorization();

        assertThat(assignment.getAuthorizationVersion()).isEqualTo(2L);
    }
}
