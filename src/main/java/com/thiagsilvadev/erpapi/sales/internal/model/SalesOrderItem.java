package com.thiagsilvadev.erpapi.sales.internal.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SalesOrderItem {

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

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPriceAmount;

    @Column(nullable = false, length = 3)
    private String currencyCode;

    protected SalesOrderItem() {}

    private SalesOrderItem(
            UUID productId,
            UUID warehouseId,
            String productSku,
            String productName,
            int quantity,
            BigDecimal unitPriceAmount,
            String currencyCode) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.productSku = productSku;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPriceAmount = unitPriceAmount;
        this.currencyCode = currencyCode;
    }

    public static SalesOrderItem create(
            UUID productId,
            UUID warehouseId,
            String productSku,
            String productName,
            int quantity,
            BigDecimal unitPriceAmount,
            String currencyCode) {
        return new SalesOrderItem(
                productId, warehouseId, productSku, productName, quantity, unitPriceAmount, currencyCode);
    }

    public BigDecimal lineTotal() {
        return unitPriceAmount.multiply(BigDecimal.valueOf(quantity));
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

    public BigDecimal getUnitPriceAmount() {
        return unitPriceAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
