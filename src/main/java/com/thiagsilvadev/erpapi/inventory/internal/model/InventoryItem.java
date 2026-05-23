package com.thiagsilvadev.erpapi.inventory.internal.model;

import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;

@Entity
@Table(
        name = "inventory_items",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_inventory_item_product_warehouse",
                    columnNames = {"productId", "warehouse_id"})
        })
public class InventoryItem extends AbstractBaseEntity {

    @Column(nullable = false)
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    private int quantityOnHand;

    @Column(nullable = false)
    private int reservedQuantity;

    protected InventoryItem() {}

    private InventoryItem(UUID productId, Warehouse warehouse, int quantityOnHand) {
        this.productId = productId;
        this.warehouse = warehouse;
        increase(quantityOnHand);
    }

    public static InventoryItem create(UUID productId, Warehouse warehouse, int quantityOnHand) {
        return new InventoryItem(productId, warehouse, quantityOnHand);
    }

    public void increase(int quantity) {
        requirePositive(quantity);
        this.quantityOnHand += quantity;
    }

    public void decrease(int quantity) {
        requirePositive(quantity);
        if (availableQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient inventory available");
        }
        this.quantityOnHand -= quantity;
    }

    public void reserve(int quantity) {
        requirePositive(quantity);
        if (availableQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient inventory available");
        }
        this.reservedQuantity += quantity;
    }

    public void release(int quantity) {
        requirePositive(quantity);
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
    }

    public void consumeReserved(int quantity) {
        requirePositive(quantity);
        if (reservedQuantity < quantity) {
            throw new IllegalArgumentException("Insufficient reserved inventory");
        }
        reservedQuantity -= quantity;
        quantityOnHand -= quantity;
    }

    public int availableQuantity() {
        return quantityOnHand - reservedQuantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
}
