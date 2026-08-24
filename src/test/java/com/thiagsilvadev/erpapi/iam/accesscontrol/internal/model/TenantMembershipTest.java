package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.INACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class TenantMembershipTest {

    @Test
    void shouldRequireUser() {
        assertThatThrownBy(() -> TenantMembership.create(null, UUID.randomUUID()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("user");
    }

    @Test
    void shouldRequireCompany() {
        assertThatThrownBy(() -> TenantMembership.create(UUID.randomUUID(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("company");
    }

    @Test
    void shouldStartActiveWithZeroAuthorizationVersion() {
        TenantMembership membership = TenantMembership.create(UUID.randomUUID(), UUID.randomUUID());

        assertThat(membership.getStatus()).isEqualTo(ACTIVE);
        assertThat(membership.getAuthorizationVersion()).isZero();
    }

    @Test
    void shouldBumpAuthorizationVersionOnActivation() {
        TenantMembership membership = TenantMembership.create(UUID.randomUUID(), UUID.randomUUID());
        membership.deactivate();

        membership.activate();

        assertThat(membership.getStatus()).isEqualTo(ACTIVE);
        assertThat(membership.getAuthorizationVersion()).isEqualTo(2L);
    }

    @Test
    void shouldBumpAuthorizationVersionOnDeactivation() {
        TenantMembership membership = TenantMembership.create(UUID.randomUUID(), UUID.randomUUID());

        membership.deactivate();

        assertThat(membership.getStatus()).isEqualTo(INACTIVE);
        assertThat(membership.getAuthorizationVersion()).isEqualTo(1L);
    }

    @Test
    void shouldBumpAuthorizationVersionOnTouch() {
        TenantMembership membership = TenantMembership.create(UUID.randomUUID(), UUID.randomUUID());

        membership.touchAuthorization();
        membership.touchAuthorization();

        assertThat(membership.getAuthorizationVersion()).isEqualTo(2L);
    }
}
