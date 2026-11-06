package com.thiagsilvadev.erpapi.financial.api;

import java.util.Set;

public final class FinancialPermissions {

    private FinancialPermissions() {}

    public static final String READ = "financial:read";
    public static final String WRITE = "financial:write";
    public static final String APPROVE_PAYMENT = "financial:approve-payment";

    public static final Set<String> ALL = Set.of(READ, WRITE, APPROVE_PAYMENT);
}
