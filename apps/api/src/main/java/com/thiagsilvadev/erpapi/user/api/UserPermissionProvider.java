package com.thiagsilvadev.erpapi.user.api;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;

@Component
public class UserPermissionProvider implements ModulePermissionProvider {

    @Override
    public Set<String> allPermissions() {
        return UserPermissions.ALL;
    }

    @Override
    public Set<String> tenantGrantablePermissions() {
        return Set.of(UserPermissions.READ, UserPermissions.WRITE, UserPermissions.DELETE);
    }

    @Override
    public Map<String, Set<String>> defaultRoleMappings() {
        return Map.of(ROLE_MANAGER, Set.of(UserPermissions.READ, UserPermissions.WRITE, UserPermissions.DELETE));
    }
}
