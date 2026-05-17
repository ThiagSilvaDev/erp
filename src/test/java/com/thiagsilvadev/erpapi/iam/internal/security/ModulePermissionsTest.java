package com.thiagsilvadev.erpapi.iam.internal.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions;
import com.thiagsilvadev.erpapi.company.api.CompanyPermissions;
import com.thiagsilvadev.erpapi.customer.api.CustomerPermissions;
import com.thiagsilvadev.erpapi.financial.api.FinancialPermissions;
import com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions;
import com.thiagsilvadev.erpapi.sales.api.SalesPermissions;
import com.thiagsilvadev.erpapi.user.api.UserPermissions;

class ModulePermissionsTest {

    @Test
    void shouldExposeCatalogPermissions() {
        assertThat(CatalogPermissions.ALL)
                .containsExactlyInAnyOrder(
                        CatalogPermissions.READ, CatalogPermissions.WRITE, CatalogPermissions.DELETE);
    }

    @Test
    void shouldExposeCompanyPermissions() {
        assertThat(CompanyPermissions.ALL)
                .containsExactlyInAnyOrder(
                        CompanyPermissions.READ, CompanyPermissions.WRITE, CompanyPermissions.DELETE);
    }

    @Test
    void shouldExposeCustomerPermissions() {
        assertThat(CustomerPermissions.ALL)
                .containsExactlyInAnyOrder(
                        CustomerPermissions.READ, CustomerPermissions.WRITE, CustomerPermissions.DELETE);
    }

    @Test
    void shouldExposeFinancialPermissions() {
        assertThat(FinancialPermissions.ALL)
                .containsExactlyInAnyOrder(
                        FinancialPermissions.READ, FinancialPermissions.WRITE, FinancialPermissions.APPROVE_PAYMENT);
    }

    @Test
    void shouldExposeInventoryPermissions() {
        assertThat(InventoryPermissions.ALL)
                .containsExactlyInAnyOrder(
                        InventoryPermissions.READ, InventoryPermissions.WRITE, InventoryPermissions.ADJUST_STOCK);
    }

    @Test
    void shouldExposeSalesPermissions() {
        assertThat(SalesPermissions.ALL)
                .containsExactlyInAnyOrder(
                        SalesPermissions.READ, SalesPermissions.WRITE, SalesPermissions.CONFIRM, SalesPermissions.CANCEL);
    }

    @Test
    void shouldExposeUserPermissions() {
        assertThat(UserPermissions.ALL)
                .containsExactlyInAnyOrder(UserPermissions.READ, UserPermissions.WRITE, UserPermissions.DELETE);
    }
}
