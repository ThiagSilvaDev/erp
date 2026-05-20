package com.thiagsilvadev.erpapi.iam.internal.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions;
import com.thiagsilvadev.erpapi.company.api.CompanyPermissions;
import com.thiagsilvadev.erpapi.customer.api.CustomerPermissions;
import com.thiagsilvadev.erpapi.financial.api.FinancialPermissions;
import com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions;
import com.thiagsilvadev.erpapi.sales.api.SalesPermissions;
import com.thiagsilvadev.erpapi.user.api.UserPermissions;

class RolePermissionResolverTest {

    private final RolePermissionResolver resolver = new RolePermissionResolver();

    @Test
    void shouldResolveAdminPermissions() {
        assertThat(resolver.resolve("ADMIN"))
                .containsAll(CatalogPermissions.ALL)
                .containsAll(CompanyPermissions.ALL)
                .containsAll(CustomerPermissions.ALL)
                .containsAll(FinancialPermissions.ALL)
                .containsAll(InventoryPermissions.ALL)
                .containsAll(LogisticsPermissions.ALL)
                .containsAll(SalesPermissions.ALL)
                .containsAll(UserPermissions.ALL)
                .doesNotContain("*");
    }

    @Test
    void shouldResolveManagerPermissions() {
        assertThat(resolver.resolve("MANAGER"))
                .containsExactlyInAnyOrder(
                        CatalogPermissions.READ,
                        CatalogPermissions.WRITE,
                        InventoryPermissions.READ,
                        InventoryPermissions.WRITE,
                        LogisticsPermissions.READ,
                        LogisticsPermissions.WRITE,
                        LogisticsPermissions.DISPATCH,
                        LogisticsPermissions.TRACK,
                        FinancialPermissions.READ);
    }

    @Test
    void shouldResolveOperatorPermissions() {
        assertThat(resolver.resolve("OPERATOR"))
                .containsExactlyInAnyOrder(
                        CatalogPermissions.READ,
                        InventoryPermissions.READ,
                        LogisticsPermissions.READ,
                        LogisticsPermissions.TRACK);
    }

    @Test
    void shouldReturnEmptyPermissionsForUnknownRoleString() {
        assertThat(resolver.resolve("UNKNOWN")).isEmpty();
    }
}
