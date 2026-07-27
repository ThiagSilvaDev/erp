package com.thiagsilvadev.erpapi.kpi.api;

import java.util.Set;

public final class KpiPermissions {

    private KpiPermissions() {}

    public static final String READ = "kpi:read";

    public static final Set<String> ALL = Set.of(READ);
}
