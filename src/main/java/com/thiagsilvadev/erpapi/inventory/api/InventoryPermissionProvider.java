package com.thiagsilvadev.erpapi.inventory.api;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;

@Component
public class InventoryPermissionProvider implements ModulePermissionProvider {

    @Override
    public Set<String> allPermissions() {
        return InventoryPermissions.ALL;
    }

    @Override
    public Map<String, Set<String>> defaultRoleMappings() {
        return Map.of(
                ModulePermissionProvider.ROLE_MANAGER, Set.of(InventoryPermissions.READ, InventoryPermissions.WRITE),
                ModulePermissionProvider.ROLE_OPERATOR, Set.of(InventoryPermissions.READ));
    }
}
