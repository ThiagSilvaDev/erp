package com.thiagsilvadev.erpapi.catalog.api;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;

@Component
public class CatalogPermissionProvider implements ModulePermissionProvider {

    @Override
    public Set<String> allPermissions() {
        return CatalogPermissions.ALL;
    }

    @Override
    public Set<String> tenantGrantablePermissions() {
        return Set.of(CatalogPermissions.READ, CatalogPermissions.WRITE);
    }

    @Override
    public Map<String, Set<String>> defaultRoleMappings() {
        return Map.of(
                ModulePermissionProvider.ROLE_MANAGER, Set.of(CatalogPermissions.READ, CatalogPermissions.WRITE),
                ModulePermissionProvider.ROLE_OPERATOR, Set.of(CatalogPermissions.READ));
    }
}
