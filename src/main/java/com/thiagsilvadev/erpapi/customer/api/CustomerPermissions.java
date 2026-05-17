package com.thiagsilvadev.erpapi.customer.api;

import java.util.Set;

public final class CustomerPermissions {

    private CustomerPermissions() {}

    public static final String READ = "customer:read";
    public static final String WRITE = "customer:write";
    public static final String DELETE = "customer:delete";

    public static final Set<String> ALL = Set.of(READ, WRITE, DELETE);
}
