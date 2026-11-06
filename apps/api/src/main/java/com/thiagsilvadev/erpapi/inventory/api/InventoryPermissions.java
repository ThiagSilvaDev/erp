package com.thiagsilvadev.erpapi.inventory.api;

import java.util.Set;

public final class InventoryPermissions {

    private InventoryPermissions() {}

    public static final String READ = "inventory:read";
    public static final String WRITE = "inventory:write";
    public static final String ADJUST_STOCK = "inventory:adjust-stock";

    public static final Set<String> ALL = Set.of(READ, WRITE, ADJUST_STOCK);
}
