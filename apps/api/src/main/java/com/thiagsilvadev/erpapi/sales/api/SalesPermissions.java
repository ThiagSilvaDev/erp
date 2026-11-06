package com.thiagsilvadev.erpapi.sales.api;

import java.util.Set;

public final class SalesPermissions {

    private SalesPermissions() {}

    public static final String READ = "sales:read";
    public static final String WRITE = "sales:write";
    public static final String CONFIRM = "sales:confirm";
    public static final String CANCEL = "sales:cancel";

    public static final Set<String> ALL = Set.of(READ, WRITE, CONFIRM, CANCEL);
}
