package com.thiagsilvadev.erpapi.iam.accesscontrol.api;

import java.util.Set;

public final class IamPermissions {

    private IamPermissions() {}

    public static final String ROLE_READ = "iam:role:read";
    public static final String ROLE_WRITE = "iam:role:write";
    public static final String ROLE_ASSIGN = "iam:role:assign";
    public static final String MEMBERSHIP_READ = "iam:membership:read";
    public static final String MEMBERSHIP_WRITE = "iam:membership:write";
    public static final String OWNERSHIP_TRANSFER = "iam:ownership:transfer";

    public static final Set<String> ALL =
            Set.of(ROLE_READ, ROLE_WRITE, ROLE_ASSIGN, MEMBERSHIP_READ, MEMBERSHIP_WRITE, OWNERSHIP_TRANSFER);
}
