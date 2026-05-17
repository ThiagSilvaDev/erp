package com.thiagsilvadev.erpapi.user.api;

import java.util.Set;

public final class UserPermissions {

    private UserPermissions() {}

    public static final String READ = "user:read";
    public static final String WRITE = "user:write";
    public static final String DELETE = "user:delete";

    public static final Set<String> ALL = Set.of(READ, WRITE, DELETE);
}
