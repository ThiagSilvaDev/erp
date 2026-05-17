package com.thiagsilvadev.erpapi.company.api;

import java.util.Set;

public final class CompanyPermissions {

    private CompanyPermissions() {}

    public static final String READ = "company:read";
    public static final String WRITE = "company:write";
    public static final String DELETE = "company:delete";

    public static final Set<String> ALL = Set.of(READ, WRITE, DELETE);
}
