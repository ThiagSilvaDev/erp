package com.thiagsilvadev.erpapi.customer.api;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;

@Component
public class CustomerPermissionProvider implements ModulePermissionProvider {

    @Override
    public Set<String> allPermissions() {
        return CustomerPermissions.ALL;
    }

    @Override
    public Set<String> tenantGrantablePermissions() {
        return Set.of(CustomerPermissions.READ, CustomerPermissions.WRITE);
    }
}
