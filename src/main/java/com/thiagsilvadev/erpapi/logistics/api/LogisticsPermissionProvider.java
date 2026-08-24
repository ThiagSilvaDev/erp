package com.thiagsilvadev.erpapi.logistics.api;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;

@Component
public class LogisticsPermissionProvider implements ModulePermissionProvider {

    @Override
    public Set<String> allPermissions() {
        return LogisticsPermissions.ALL;
    }

    @Override
    public Map<String, Set<String>> defaultRoleMappings() {
        return Map.of(
                ModulePermissionProvider.ROLE_MANAGER,
                Set.of(
                        LogisticsPermissions.READ,
                        LogisticsPermissions.WRITE,
                        LogisticsPermissions.DISPATCH,
                        LogisticsPermissions.TRACK),
                ModulePermissionProvider.ROLE_OPERATOR,
                Set.of(LogisticsPermissions.READ, LogisticsPermissions.TRACK),
                ModulePermissionProvider.ROLE_DRIVER,
                Set.of(LogisticsPermissions.DRIVER_WORKFLOW));
    }
}
