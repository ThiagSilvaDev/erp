package com.thiagsilvadev.erpapi.logistics.internal.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Embeddable
public class ShipmentItem {

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private UUID warehouseId;

    @Column(nullable = false)
    private String productSku;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    protected ShipmentItem() {}

    private ShipmentItem(UUID productId, UUID warehouseId, String productSku, String productName, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Shipment item quantity must be positive");
        }
        this.productId = DomainAssert.requireNotNull(productId, "Product");
        this.warehouseId = DomainAssert.requireNotNull(warehouseId, "Warehouse");
        this.productSku = Strings.required(productSku, "Product SKU");
        this.productName = Strings.required(productName, "Product name");
        this.quantity = quantity;
    }

    public static ShipmentItem create(
            UUID productId, UUID warehouseId, String productSku, String productName, int quantity) {
        return new ShipmentItem(productId, warehouseId, productSku, productName, quantity);
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getWarehouseId() {
        return warehouseId;
    }

    public String getProductSku() {
        return productSku;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }
}
