package com.thiagsilvadev.erpapi.sales.internal.model;

import java.math.BigDecimal;
import java.util.*;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.sales.api.FulfillmentType;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_type", nullable = false)
    private FulfillmentType fulfillmentType;

    @Embedded
    private DeliveryRoute deliveryRoute;

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
            FulfillmentType fulfillmentType,
            DeliveryRoute deliveryRoute,
            UUID sellerCompanyId,
            UUID buyerCompanyId,
            UUID customerId,
            List<SalesOrderItem> items) {
        this.orderNumber = Objects.requireNonNull(orderNumber, "Order number is required");
        this.saleType = Objects.requireNonNull(saleType, "Sale type is required");
        this.fulfillmentType = Objects.requireNonNull(fulfillmentType, "Fulfillment type is required");
        this.deliveryRoute = deliveryRoute;
        this.sellerCompanyId = Objects.requireNonNull(sellerCompanyId, "Seller company is required");
        this.buyerCompanyId = buyerCompanyId;
        this.customerId = customerId;
        this.items.addAll(items);
    }

    public static SalesOrder create(
            Long orderNumber,
            SaleType saleType,
            FulfillmentType fulfillmentType,
            DeliveryRoute deliveryRoute,
            UUID sellerCompanyId,
            UUID buyerCompanyId,
            UUID customerId,
            List<SalesOrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Sales order must have at least one item");
        }

        validateFulfillment(fulfillmentType, deliveryRoute, items);

        return new SalesOrder(
                orderNumber,
                saleType,
                fulfillmentType,
                deliveryRoute,
                sellerCompanyId,
                buyerCompanyId,
                customerId,
                items);
    }

    private static void validateFulfillment(
            FulfillmentType fulfillmentType, DeliveryRoute deliveryRoute, List<SalesOrderItem> items) {
        if (fulfillmentType == FulfillmentType.DELIVERY && deliveryRoute == null) {
            throw new IllegalArgumentException("Delivery route is required for delivery orders.");
        }

        if (fulfillmentType == FulfillmentType.PICKUP && deliveryRoute != null) {
            throw new IllegalArgumentException("Pickup orders must not have delivery route.");
        }

        if (fulfillmentType == FulfillmentType.DELIVERY) {
            boolean hasDifferentWarehouse = items.stream()
                    .anyMatch(item -> !item.getWarehouseId().equals(deliveryRoute.getOriginWarehouseId()));

            if (hasDifferentWarehouse) {
                throw new IllegalArgumentException(
                        "Delivery orders currently support items from only one origin warehouse.");
            }
        }
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

    public boolean isDelivery() {
        return fulfillmentType == FulfillmentType.DELIVERY;
    }

    public boolean isPickup() {
        return fulfillmentType == FulfillmentType.PICKUP;
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

    public FulfillmentType getFulfillmentType() {
        return fulfillmentType;
    }

    public DeliveryRoute getDeliveryRoute() {
        return deliveryRoute;
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
