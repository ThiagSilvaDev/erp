package com.thiagsilvadev.erpapi.financial.api;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;

@Component
public class FinancialPermissionProvider implements ModulePermissionProvider {

    @Override
    public Set<String> allPermissions() {
        return FinancialPermissions.ALL;
    }

    @Override
    public Set<String> tenantGrantablePermissions() {
        return Set.of(FinancialPermissions.READ, FinancialPermissions.WRITE);
    }

    @Override
    public Map<String, Set<String>> defaultRoleMappings() {
        return Map.of(ModulePermissionProvider.ROLE_MANAGER, Set.of(FinancialPermissions.READ));
    }
}
