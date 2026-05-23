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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockMovementType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockMovementReason reason;

    protected StockMovement() {}

    private StockMovement(
            Long movementNumber,
            UUID productId,
            UUID warehouseId,
            int quantity,
            StockMovementType type,
            StockMovementReason reason) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.movementNumber = movementNumber;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.type = java.util.Objects.requireNonNull(type, "Stock movement type is required");
        this.reason = java.util.Objects.requireNonNull(reason, "Stock movement reason is required");
    }

    public static StockMovement create(
            Long movementNumber,
            UUID productId,
            UUID warehouseId,
            int quantity,
            StockMovementType type,
            StockMovementReason reason) {
        return new StockMovement(movementNumber, productId, warehouseId, quantity, type, reason);
    }

    public static StockMovement createIn(
            Long movementNumber, UUID productId, UUID warehouseId, int quantity, StockMovementReason reason) {
        return create(movementNumber, productId, warehouseId, quantity, StockMovementType.IN, reason);
    }

    public static StockMovement createOut(
            Long movementNumber, UUID productId, UUID warehouseId, int quantity, StockMovementReason reason) {
        return create(movementNumber, productId, warehouseId, quantity, StockMovementType.OUT, reason);
    }

    public static StockMovement createAdjustment(
            Long movementNumber, UUID productId, UUID warehouseId, int quantity, StockMovementReason reason) {
        return create(movementNumber, productId, warehouseId, quantity, StockMovementType.ADJUSTMENT, reason);
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

    public StockMovementType getType() {
        return type;
    }

    public StockMovementReason getReason() {
        return reason;
    }
}
