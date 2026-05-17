package com.thiagsilvadev.erpapi.iam.internal.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.user.api.UserRole;

class RolePermissionResolverTest {

    private final RolePermissionResolver resolver = new RolePermissionResolver();

    @Test
    void shouldResolveAdminPermissions() {
        assertThat(resolver.resolve(UserRole.ADMIN)).containsExactly("*");
    }

    @Test
    void shouldResolveManagerPermissions() {
        assertThat(resolver.resolve(UserRole.MANAGER))
                .containsExactlyInAnyOrder(
                        "catalog:read", "catalog:write", "inventory:read", "inventory:write", "financial:read");
    }

    @Test
    void shouldResolveOperatorPermissions() {
        assertThat(resolver.resolve(UserRole.OPERATOR)).containsExactlyInAnyOrder("catalog:read", "inventory:read");
    }

    @Test
    void shouldReturnEmptyPermissionsForUnknownRoleString() {
        assertThat(resolver.resolve("UNKNOWN")).isEmpty();
    }
}
