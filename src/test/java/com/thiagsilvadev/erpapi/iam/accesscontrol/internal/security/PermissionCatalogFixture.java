package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security;

import java.util.List;

import com.thiagsilvadev.erpapi.catalog.api.CatalogPermissionProvider;
import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;
import com.thiagsilvadev.erpapi.company.api.CompanyPermissionProvider;
import com.thiagsilvadev.erpapi.customer.api.CustomerPermissionProvider;
import com.thiagsilvadev.erpapi.financial.api.FinancialPermissionProvider;
import com.thiagsilvadev.erpapi.inventory.api.InventoryPermissionProvider;
import com.thiagsilvadev.erpapi.kpi.api.KpiPermissionProvider;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissionProvider;
import com.thiagsilvadev.erpapi.sales.api.SalesPermissionProvider;
import com.thiagsilvadev.erpapi.user.api.UserPermissionProvider;

public final class PermissionCatalogFixture {

    private PermissionCatalogFixture() {}

    public static PermissionCatalog catalog() {
        return new PermissionCatalog(providers());
    }

    public static List<ModulePermissionProvider> providers() {
        return List.of(
                new CatalogPermissionProvider(),
                new CompanyPermissionProvider(),
                new CustomerPermissionProvider(),
                new FinancialPermissionProvider(),
                new InventoryPermissionProvider(),
                new KpiPermissionProvider(),
                new LogisticsPermissionProvider(),
                new SalesPermissionProvider(),
                new UserPermissionProvider());
    }
}
