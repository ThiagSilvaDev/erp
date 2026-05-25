package com.thiagsilvadev.erpapi.sales.internal.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.model.Money;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.sales.internal.exception.SalesOrderItemAssignmentException;
import com.thiagsilvadev.erpapi.sales.internal.exception.SalesOrderItemErrorCode;

@Entity
@Table(name = "sales_order_items")
public class SalesOrderItem extends AbstractBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_order_id", nullable = false, updatable = false)
    private SalesOrder salesOrder;

    @Column(nullable = false, updatable = false)
    private UUID productId;

    @Column(nullable = false, updatable = false)
    private UUID warehouseId;

    @Column(nullable = false, updatable = false)
    private String productSku;

    @Column(nullable = false, updatable = false)
    private String productName;

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Embedded
    @AttributeOverride(
            name = "amount",
            column =
                    @Column(name = "unit_price_amount", nullable = false, precision = 19, scale = 2, updatable = false))
    @AttributeOverride(
            name = "currencyCode",
            column = @Column(name = "currency_code", nullable = false, length = 3, updatable = false))
    private Money unitPrice;

    protected SalesOrderItem() {}

    private SalesOrderItem(
            UUID productId, UUID warehouseId, String productSku, String productName, int quantity, Money unitPrice) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.productSku = productSku;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public static SalesOrderItem create(
            UUID productId, UUID warehouseId, String productSku, String productName, int quantity, Money unitPrice) {
        return new SalesOrderItem(
                DomainAssert.requireNonNull(productId, SalesOrderItemErrorCode.PRODUCT_ID_REQUIRED),
                DomainAssert.requireNonNull(warehouseId, SalesOrderItemErrorCode.WAREHOUSE_ID_REQUIRED),
                DomainAssert.requireNonBlank(productSku, SalesOrderItemErrorCode.PRODUCT_SKU_REQUIRED),
                DomainAssert.requireNonBlank(productName, SalesOrderItemErrorCode.PRODUCT_NAME_REQUIRED),
                DomainAssert.requirePositive(quantity, SalesOrderItemErrorCode.QUANTITY_MUST_BE_POSITIVE),
                DomainAssert.requireNonNull(unitPrice, SalesOrderItemErrorCode.UNIT_PRICE_REQUIRED));
    }

    void assignTo(SalesOrder salesOrder) {
        if (this.salesOrder != null && this.salesOrder != salesOrder) {
            throw new SalesOrderItemAssignmentException(SalesOrderItemErrorCode.ALREADY_ASSIGNED);
        }
        this.salesOrder = DomainAssert.requireNonNull(salesOrder, SalesOrderItemErrorCode.ORDER_REQUIRED);
    }

    void unassignFrom(SalesOrder salesOrder) {
        if (this.salesOrder != salesOrder) {
            throw new SalesOrderItemAssignmentException(SalesOrderItemErrorCode.NOT_ASSIGNED_TO_ORDER);
        }
        this.salesOrder = null;
    }

    public BigDecimal lineTotal() {
        return unitPrice.getAmount().multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getProductId() {
        return productId;
    }

    public SalesOrder getSalesOrder() {
        return salesOrder;
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
        return unitPrice.getAmount();
    }

    public String getCurrencyCode() {
        return unitPrice.getCurrencyCode();
    }

    public Money getUnitPrice() {
        return unitPrice;
    }
}
