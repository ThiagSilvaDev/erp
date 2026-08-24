package com.thiagsilvadev.erpapi.sales.api;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;

@Component
public class SalesPermissionProvider implements ModulePermissionProvider {

    @Override
    public Set<String> allPermissions() {
        return SalesPermissions.ALL;
    }
}
