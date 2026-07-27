package com.thiagsilvadev.erpapi.iam.internal.security;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions;
import com.thiagsilvadev.erpapi.company.api.CompanyPermissions;
import com.thiagsilvadev.erpapi.customer.api.CustomerPermissions;
import com.thiagsilvadev.erpapi.financial.api.FinancialPermissions;
import com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions;
import com.thiagsilvadev.erpapi.kpi.api.KpiPermissions;
import com.thiagsilvadev.erpapi.logistics.api.LogisticsPermissions;
import com.thiagsilvadev.erpapi.sales.api.SalesPermissions;

@Component
public class RolePermissionResolver {

    private static final Set<String> USER_PERMISSIONS = Set.of("user:read", "user:write", "user:delete");

    public Set<String> resolve(String role) {
        return switch (normalize(role)) {
            case "ADMIN" -> allPermissions();
            case "MANAGER" ->
                Set.of(
                        CatalogPermissions.READ,
                        CatalogPermissions.WRITE,
                        InventoryPermissions.READ,
                        InventoryPermissions.WRITE,
                        LogisticsPermissions.READ,
                        LogisticsPermissions.WRITE,
                        LogisticsPermissions.DISPATCH,
                        LogisticsPermissions.TRACK,
                        FinancialPermissions.READ,
                        KpiPermissions.READ);
            case "OPERATOR" ->
                Set.of(
                        CatalogPermissions.READ,
                        InventoryPermissions.READ,
                        LogisticsPermissions.READ,
                        LogisticsPermissions.TRACK);
            default -> Set.of();
        };
    }

    private static String normalize(String role) {
        if (role == null || role.isBlank()) {
            return "";
        }
        return role.trim().toUpperCase(Locale.ROOT);
    }

    private static Set<String> allPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.addAll(CatalogPermissions.ALL);
        permissions.addAll(CompanyPermissions.ALL);
        permissions.addAll(CustomerPermissions.ALL);
        permissions.addAll(FinancialPermissions.ALL);
        permissions.addAll(InventoryPermissions.ALL);
        permissions.addAll(KpiPermissions.ALL);
        permissions.addAll(LogisticsPermissions.ALL);
        permissions.addAll(SalesPermissions.ALL);
        permissions.addAll(USER_PERMISSIONS);
        return Set.copyOf(permissions);
    }
}
