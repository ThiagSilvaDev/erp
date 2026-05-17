package com.thiagsilvadev.erpapi.iam.internal.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions;
import com.thiagsilvadev.erpapi.financial.api.FinancialPermissions;
import com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions;
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
                        CatalogPermissions.READ,
                        CatalogPermissions.WRITE,
                        InventoryPermissions.READ,
                        InventoryPermissions.WRITE,
                        FinancialPermissions.READ);
    }

    @Test
    void shouldResolveOperatorPermissions() {
        assertThat(resolver.resolve(UserRole.OPERATOR))
                .containsExactlyInAnyOrder(CatalogPermissions.READ, InventoryPermissions.READ);
    }

    @Test
    void shouldReturnEmptyPermissionsForUnknownRoleString() {
        assertThat(resolver.resolve("UNKNOWN")).isEmpty();
    }
}
