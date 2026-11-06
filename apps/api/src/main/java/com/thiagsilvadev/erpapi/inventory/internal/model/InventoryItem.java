package com.thiagsilvadev.erpapi.inventory.internal.model;

import java.util.UUID;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.inventory.internal.exception.InsufficientInventoryAvailableException;
import com.thiagsilvadev.erpapi.inventory.internal.exception.InsufficientReservedInventoryException;
import com.thiagsilvadev.erpapi.inventory.internal.exception.InventoryErrorCode;

@Entity
@Table(
        name = "inventory_items",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_inventory_item_company_product_warehouse",
                    columnNames = {"company_id", "product_id", "warehouse_id"})
        })
public class InventoryItem extends AbstractBaseEntity {

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false, updatable = false)
    private Warehouse warehouse;

    @Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    protected InventoryItem() {}

    private InventoryItem(UUID productId, Warehouse warehouse, int quantityOnHand) {
        this.companyId = DomainAssert.requireNotNull(warehouse, "Warehouse").getCompanyId();
        this.productId = DomainAssert.requireNotNull(productId, "Product ID");
        this.warehouse = warehouse;
        this.quantityOnHand = DomainAssert.requireNonNegative(
                quantityOnHand, InventoryErrorCode.QUANTITY_ON_HAND_MUST_BE_NON_NEGATIVE);
    }

    public static InventoryItem create(UUID productId, Warehouse warehouse, int quantityOnHand) {
        return new InventoryItem(productId, warehouse, quantityOnHand);
    }

    public void increase(int quantity) {
        DomainAssert.requirePositive(quantity, InventoryErrorCode.QUANTITY_MUST_BE_POSITIVE);
        this.quantityOnHand += quantity;
    }

    public void decrease(int quantity) {
        DomainAssert.requirePositive(quantity, InventoryErrorCode.QUANTITY_MUST_BE_POSITIVE);
        requireAvailable(quantity);
        this.quantityOnHand -= quantity;
    }

    public void reserve(int quantity) {
        DomainAssert.requirePositive(quantity, InventoryErrorCode.QUANTITY_MUST_BE_POSITIVE);
        requireAvailable(quantity);
        this.reservedQuantity += quantity;
    }

    public void release(int quantity) {
        DomainAssert.requirePositive(quantity, InventoryErrorCode.QUANTITY_MUST_BE_POSITIVE);
        if (this.reservedQuantity < quantity) {
            throw new InsufficientReservedInventoryException();
        }
        this.reservedQuantity -= quantity;
    }

    public void consumeReserved(int quantity) {
        DomainAssert.requirePositive(quantity, InventoryErrorCode.QUANTITY_MUST_BE_POSITIVE);
        if (this.reservedQuantity < quantity) {
            throw new InsufficientReservedInventoryException();
        }
        this.reservedQuantity -= quantity;
        this.quantityOnHand -= quantity;
    }

    public int availableQuantity() {
        return this.quantityOnHand - this.reservedQuantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getCompanyId() {
        return companyId;
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

    private void requireAvailable(int quantity) {
        if (availableQuantity() < quantity) {
            throw new InsufficientInventoryAvailableException();
        }
    }
}
