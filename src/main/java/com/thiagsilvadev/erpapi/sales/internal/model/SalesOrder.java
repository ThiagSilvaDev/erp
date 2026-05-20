package com.thiagsilvadev.erpapi.sales.internal.model;

import java.math.BigDecimal;
import java.util.*;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.sales.api.OrderStatus;
import com.thiagsilvadev.erpapi.sales.api.SaleType;

@Entity
@Table(
        name = "sales_orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_sales_order_order_number", columnNames = "order_number"))
public class SalesOrder extends AbstractBaseEntity {

    @Column(name = "order_number", nullable = false, updatable = false)
    private Long orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleType saleType;

    @Column(nullable = false)
    private UUID sellerCompanyId;

    @Column
    private UUID buyerCompanyId;

    @Column
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.DRAFT;

    @ElementCollection
    @CollectionTable(name = "sales_order_items", joinColumns = @JoinColumn(name = "sales_order_id"))
    private List<SalesOrderItem> items = new ArrayList<>();

    protected SalesOrder() {}

    private SalesOrder(
            Long orderNumber,
            SaleType saleType,
            UUID sellerCompanyId,
            UUID buyerCompanyId,
            UUID customerId,
            List<SalesOrderItem> items) {
        this.orderNumber = Objects.requireNonNull(orderNumber, "Order number is required");
        this.saleType = Objects.requireNonNull(saleType, "Sale type is required");
        this.sellerCompanyId = Objects.requireNonNull(sellerCompanyId, "Seller company is required");
        this.buyerCompanyId = buyerCompanyId;
        this.customerId = customerId;
        this.items.addAll(items);
    }

    public static SalesOrder create(
            Long orderNumber,
            SaleType saleType,
            UUID sellerCompanyId,
            UUID buyerCompanyId,
            UUID customerId,
            List<SalesOrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Sales order must have at least one item");
        }
        return new SalesOrder(orderNumber, saleType, sellerCompanyId, buyerCompanyId, customerId, items);
    }

    public void confirm() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Only draft orders can be confirmed");
        }
        status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            return;
        }
        status = OrderStatus.CANCELLED;
    }

    public BigDecimal totalAmount() {
        return items.stream().map(SalesOrderItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String currencyCode() {
        return items.getFirst().getCurrencyCode();
    }

    public SaleType getSaleType() {
        return saleType;
    }

    public UUID getSellerCompanyId() {
        return sellerCompanyId;
    }

    public UUID getBuyerCompanyId() {
        return buyerCompanyId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<SalesOrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
