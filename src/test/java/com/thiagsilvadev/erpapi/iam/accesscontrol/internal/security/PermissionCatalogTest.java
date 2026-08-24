package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions;
import com.thiagsilvadev.erpapi.company.api.CompanyPermissions;
import com.thiagsilvadev.erpapi.customer.api.CustomerPermissions;
import com.thiagsilvadev.erpapi.financial.api.FinancialPermissions;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.IamPermissions;
import com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions;
import com.thiagsilvadev.erpapi.kpi.api.KpiPermissions;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions;
import com.thiagsilvadev.erpapi.sales.api.SalesPermissions;
import com.thiagsilvadev.erpapi.user.api.UserPermissions;

class PermissionCatalogTest {

    private final PermissionCatalog catalog = PermissionCatalogFixture.catalog();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldExposePlatformPermissionsWithEveryModulePermission() {
        assertThat(catalog.platformPermissions())
                .contains(PermissionCatalog.PLATFORM_COMPANY_READ, PermissionCatalog.PLATFORM_COMPANY_WRITE)
                .containsAll(CatalogPermissions.ALL)
                .containsAll(CompanyPermissions.ALL)
                .containsAll(CustomerPermissions.ALL)
                .containsAll(FinancialPermissions.ALL)
                .containsAll(InventoryPermissions.ALL)
                .containsAll(KpiPermissions.ALL)
                .containsAll(LogisticsPermissions.ALL)
                .containsAll(SalesPermissions.ALL)
                .containsAll(UserPermissions.ALL)
                .doesNotContainAnyElementsOf(IamPermissions.ALL);
    }

    @Test
    void shouldRejectPermissionsOutsideTenantScope() {
        assertThatThrownBy(() -> catalog.validateTenantPermissions(Set.of(CatalogPermissions.DELETE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("catalog:delete");
    }

    @Test
    void shouldAcceptAllTenantPermissions() {
        catalog.validateTenantPermissions(catalog.tenantPermissions());
    }

    @Test
    void shouldAcceptGrantablePermissionsTheCallerPossesses() {
        authenticate(CatalogPermissions.READ, CatalogPermissions.WRITE);

        catalog.validateGrantable(Set.of(CatalogPermissions.READ));
    }

    @Test
    void shouldRejectGrantablePermissionsTheCallerDoesNotPossess() {
        authenticate(CatalogPermissions.READ);

        assertThatThrownBy(() -> catalog.validateGrantable(Set.of(CatalogPermissions.READ, CatalogPermissions.WRITE)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("caller does not possess");
    }

    @Test
    void shouldRejectGrantablePermissionsWhenUnauthenticated() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> catalog.validateGrantable(Set.of(CatalogPermissions.READ)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("caller does not possess");
    }

    @Test
    void shouldRejectUnknownGrantablePermissions() {
        authenticate(CatalogPermissions.READ);

        assertThatThrownBy(() -> catalog.validateGrantable(Set.of("unknown:permission")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown or non-tenant permissions");
    }

    private static void authenticate(String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", null, authorities));
    }
}
