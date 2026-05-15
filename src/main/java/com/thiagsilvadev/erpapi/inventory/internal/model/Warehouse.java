package com.thiagsilvadev.erpapi.inventory.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.common.model.AbstractBaseEntity;

@Entity
@Table(
        name = "inventory_warehouses",
        uniqueConstraints = {@UniqueConstraint(name = "uk_inventory_warehouse_code", columnNames = "code")})
public class Warehouse extends AbstractBaseEntity {

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false)
    private String name;

    protected Warehouse() {}

    private Warehouse(String code, String name) {
        this.code = normalizeCode(code);
        this.name = required(name, "Warehouse name cannot be blank");
    }

    public static Warehouse create(String code, String name) {
        return new Warehouse(code, name);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String normalizeCode(String code) {
        return required(code, "Warehouse code cannot be blank").toUpperCase();
    }

    static String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
