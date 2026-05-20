package com.thiagsilvadev.erpapi.logistics.api;

import java.util.Set;

public final class LogisticsPermissions {

    private LogisticsPermissions() {}

    public static final String READ = "logistics:read";
    public static final String WRITE = "logistics:write";
    public static final String DISPATCH = "logistics:dispatch";
    public static final String TRACK = "logistics:track";

    public static final Set<String> ALL = Set.of(READ, WRITE, DISPATCH, TRACK);
}
