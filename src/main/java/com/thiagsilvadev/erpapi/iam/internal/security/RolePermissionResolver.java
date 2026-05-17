package com.thiagsilvadev.erpapi.iam.internal.security;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions;
import com.thiagsilvadev.erpapi.company.api.CompanyPermissions;
import com.thiagsilvadev.erpapi.customer.api.CustomerPermissions;
import com.thiagsilvadev.erpapi.financial.api.FinancialPermissions;
import com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions;
import com.thiagsilvadev.erpapi.sales.api.SalesPermissions;
import com.thiagsilvadev.erpapi.user.api.UserPermissions;
import com.thiagsilvadev.erpapi.user.api.UserRole;

@Component
public class RolePermissionResolver {

    public Set<String> resolve(UserRole role) {
        return switch (role) {
            case ADMIN -> allPermissions();
            case MANAGER ->
                Set.of(
                        CatalogPermissions.READ,
                        CatalogPermissions.WRITE,
                        InventoryPermissions.READ,
                        InventoryPermissions.WRITE,
                        FinancialPermissions.READ);
            case OPERATOR -> Set.of(CatalogPermissions.READ, InventoryPermissions.READ);
        };
    }

    public Set<String> resolve(String role) {
        return parse(role).map(this::resolve).orElseGet(Set::of);
    }

    private static Optional<UserRole> parse(String role) {
        if (role == null || role.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static Set<String> allPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.addAll(CatalogPermissions.ALL);
        permissions.addAll(CompanyPermissions.ALL);
        permissions.addAll(CustomerPermissions.ALL);
        permissions.addAll(FinancialPermissions.ALL);
        permissions.addAll(InventoryPermissions.ALL);
        permissions.addAll(SalesPermissions.ALL);
        permissions.addAll(UserPermissions.ALL);
        return Set.copyOf(permissions);
    }
}
