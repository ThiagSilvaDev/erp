package com.thiagsilvadev.erpapi.inventory.internal.model;

import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.model.AbstractBaseEntity;

@Entity
@Table(name = "inventory_reservations")
public class InventoryReservation extends AbstractBaseEntity {

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private UUID warehouseId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private UUID salesOrderId;

    protected InventoryReservation() {}

    private InventoryReservation(UUID productId, UUID warehouseId, int quantity, UUID salesOrderId) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.salesOrderId = salesOrderId;
    }

    public static InventoryReservation create(UUID productId, UUID warehouseId, int quantity, UUID salesOrderId) {
        return new InventoryReservation(productId, warehouseId, quantity, salesOrderId);
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getWarehouseId() {
        return warehouseId;
    }

    public int getQuantity() {
        return quantity;
    }

    public UUID getSalesOrderId() {
        return salesOrderId;
    }
}
