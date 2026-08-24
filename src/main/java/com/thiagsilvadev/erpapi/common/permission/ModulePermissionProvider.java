package com.thiagsilvadev.erpapi.common.permission;

import java.util.Map;
import java.util.Set;

/**
 * SPI contract that decouples the IAM module from the modules that own permissions. Every module
 * with permissions provides a Spring bean implementing this interface; the IAM aggregates all beans
 * instead of importing module classes directly.
 */
public interface ModulePermissionProvider {

    String ROLE_OWNER = "OWNER";
    String ROLE_ADMIN = "ADMIN";
    String ROLE_MANAGER = "MANAGER";
    String ROLE_OPERATOR = "OPERATOR";
    String ROLE_DRIVER = "DRIVER";

    /** Every permission the module owns, including platform-scope permissions. */
    Set<String> allPermissions();

    /** Permissions a tenant role may grant; defaults to {@link #allPermissions()}. */
    default Set<String> tenantGrantablePermissions() {
        return allPermissions();
    }

    /** Module contribution to the built-in role templates, keyed by default role code. */
    default Map<String, Set<String>> defaultRoleMappings() {
        return Map.of();
    }
}
