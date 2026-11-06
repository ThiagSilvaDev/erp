package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;

class TenantRoleTest {

    @Test
    void shouldRejectUpdateOnBuiltInRole() {
        TenantRole role = TenantRole.builtIn(UUID.randomUUID(), "OWNER", "Owner", null);

        assertThatThrownBy(() -> role.update("New name", "New description"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Built-in roles cannot be edited");
    }

    @Test
    void shouldRejectDeactivationOnBuiltInRole() {
        TenantRole role = TenantRole.builtIn(UUID.randomUUID(), "ADMIN", "Admin", null);

        assertThatThrownBy(role::deactivate)
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Built-in roles cannot be deleted");
    }

    @Test
    void shouldNormalizeBuiltInCode() {
        TenantRole role = TenantRole.builtIn(UUID.randomUUID(), " owner ", "Owner", null);

        assertThat(role.getCode()).isEqualTo("OWNER");
    }

    @Test
    void shouldUpdateCustomRole() {
        TenantRole role = TenantRole.custom(UUID.randomUUID(), "Analyst", "old description");

        role.update("Senior Analyst", "new description");

        assertThat(role.getName()).isEqualTo("Senior Analyst");
        assertThat(role.getDescription()).isEqualTo("new description");
    }

    @Test
    void shouldRejectUpdateOnDeletedRole() {
        TenantRole role = TenantRole.custom(UUID.randomUUID(), "Analyst", null);
        role.deactivate();

        assertThatThrownBy(() -> role.update("Renamed", "new description"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Deleted roles cannot be edited");
    }

    @Test
    void shouldDeactivateCustomRole() {
        TenantRole role = TenantRole.custom(UUID.randomUUID(), "Analyst", null);

        role.deactivate();

        assertThat(role.getStatus()).isEqualTo(AuthorizationStatus.INACTIVE);
    }
}
