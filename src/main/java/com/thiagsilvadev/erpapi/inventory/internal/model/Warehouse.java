package com.thiagsilvadev.erpapi.inventory.internal.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Entity
@Table(
        name = "inventory_warehouses",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_inventory_warehouse_company_code",
                    columnNames = {"company_id", "code"})
        })
public class Warehouse extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false)
    private String name;

    protected Warehouse() {}

    private Warehouse(UUID companyId, String code, String name) {
        this.companyId = java.util.Objects.requireNonNull(companyId, "Company ID is required");
        this.code = normalizeCode(code);
        this.name = Strings.required(name, "Warehouse name cannot be blank");
    }

    public static Warehouse create(UUID companyId, String code, String name) {
        return new Warehouse(companyId, code, name);
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String normalizeCode(String code) {
        return Strings.required(code, "Warehouse code cannot be blank").toUpperCase();
    }
}
