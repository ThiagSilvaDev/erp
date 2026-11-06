package com.thiagsilvadev.erpapi.catalog.api;

import java.util.Set;

public final class CatalogPermissions {

    private CatalogPermissions() {}

    public static final String READ = "catalog:read";
    public static final String WRITE = "catalog:write";
    public static final String DELETE = "catalog:delete";

    public static final Set<String> ALL = Set.of(READ, WRITE, DELETE);
}
