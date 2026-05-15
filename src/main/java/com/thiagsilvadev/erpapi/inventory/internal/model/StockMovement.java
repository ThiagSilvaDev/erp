package com.thiagsilvadev.erpapi.inventory.internal.model;

import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.model.AbstractBaseEntity;

@Entity
@Table(name = "inventory_stock_movements")
public class StockMovement extends AbstractBaseEntity {

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private UUID warehouseId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String reason;

    protected StockMovement() {}

    private StockMovement(UUID productId, UUID warehouseId, int quantity, String reason) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.reason = Warehouse.required(reason, "Stock movement reason cannot be blank");
    }

    public static StockMovement create(UUID productId, UUID warehouseId, int quantity, String reason) {
        return new StockMovement(productId, warehouseId, quantity, reason);
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

    public String getReason() {
        return reason;
    }
}
