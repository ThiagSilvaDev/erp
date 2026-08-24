package com.thiagsilvadev.erpapi.kpi.api;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;

@Component
public class KpiPermissionProvider implements ModulePermissionProvider {

    @Override
    public Set<String> allPermissions() {
        return KpiPermissions.ALL;
    }

    @Override
    public Map<String, Set<String>> defaultRoleMappings() {
        return Map.of(ModulePermissionProvider.ROLE_MANAGER, Set.of(KpiPermissions.READ));
    }
}
