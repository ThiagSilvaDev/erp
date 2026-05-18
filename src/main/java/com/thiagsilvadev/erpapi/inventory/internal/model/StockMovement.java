package com.thiagsilvadev.erpapi.inventory.internal.model;

import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;

@Entity
@Table(
        name = "inventory_stock_movements",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_inventory_stock_movement_movement_number",
                        columnNames = "movement_number"))
public class StockMovement extends AbstractBaseEntity {

    @Column(name = "movement_number", nullable = false, updatable = false)
    private Long movementNumber;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private UUID warehouseId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String reason;

    protected StockMovement() {}

    private StockMovement(Long movementNumber, UUID productId, UUID warehouseId, int quantity, String reason) {
        this.movementNumber = movementNumber;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.reason = Warehouse.required(reason, "Stock movement reason cannot be blank");
    }

    public static StockMovement create(
            Long movementNumber, UUID productId, UUID warehouseId, int quantity, String reason) {
        return new StockMovement(movementNumber, productId, warehouseId, quantity, reason);
    }

    public Long getMovementNumber() {
        return movementNumber;
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
