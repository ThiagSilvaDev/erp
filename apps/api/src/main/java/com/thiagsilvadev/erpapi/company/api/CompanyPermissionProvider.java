package com.thiagsilvadev.erpapi.company.api;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;

@Component
public class CompanyPermissionProvider implements ModulePermissionProvider {

    @Override
    public Set<String> allPermissions() {
        return CompanyPermissions.ALL;
    }

    @Override
    public Set<String> tenantGrantablePermissions() {
        return Set.of(CompanyPermissions.READ, CompanyPermissions.WRITE);
    }
}
