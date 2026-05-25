package com.thiagsilvadev.erpapi.sales.internal.model;

import java.math.BigDecimal;
import java.util.*;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.sales.api.FulfillmentType;
import com.thiagsilvadev.erpapi.sales.api.OrderStatus;
import com.thiagsilvadev.erpapi.sales.api.PaymentMethod;
import com.thiagsilvadev.erpapi.sales.api.SaleType;
import com.thiagsilvadev.erpapi.sales.internal.exception.DeliveryRouteErrorCode;
import com.thiagsilvadev.erpapi.sales.internal.exception.SalesOrderErrorCode;
import com.thiagsilvadev.erpapi.sales.internal.exception.SalesOrderException;

@Entity
@Table(
        name = "sales_orders",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_sales_order_order_number",
                    columnNames = {"order_number"})
        })
public class SalesOrder extends AbstractBaseEntity {

    @Column(name = "order_number", nullable = false, updatable = false)
    private Long orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private SaleType saleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_type", nullable = false, updatable = false)
    private FulfillmentType fulfillmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30, updatable = false)
    private PaymentMethod paymentMethod;

    @Embedded
    private DeliveryRoute deliveryRoute;

    @Column(nullable = false, updatable = false)
    private UUID sellerCompanyId;

    @Column(updatable = false)
    private UUID buyerCompanyId;

    @Column(updatable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.DRAFT;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SalesOrderItem> items = new LinkedHashSet<>();

    protected SalesOrder() {}

    private SalesOrder(
            Long orderNumber,
            SaleType saleType,
            FulfillmentType fulfillmentType,
            PaymentMethod paymentMethod,
            DeliveryRoute deliveryRoute,
            UUID sellerCompanyId,
            UUID buyerCompanyId,
            UUID customerId) {
        this.orderNumber = orderNumber;
        this.saleType = saleType;
        this.fulfillmentType = fulfillmentType;
        this.paymentMethod = paymentMethod;
        this.deliveryRoute = deliveryRoute;
        this.sellerCompanyId = sellerCompanyId;
        this.buyerCompanyId = buyerCompanyId;
        this.customerId = customerId;
    }

    public static SalesOrder createB2B(
            Long orderNumber,
            FulfillmentType fulfillmentType,
            PaymentMethod paymentMethod,
            DeliveryRoute deliveryRoute,
            UUID sellerCompanyId,
            UUID buyerCompanyId,
            Set<SalesOrderItem> items) {
        return create(
                orderNumber,
                SaleType.B2B,
                fulfillmentType,
                paymentMethod,
                deliveryRoute,
                sellerCompanyId,
                buyerCompanyId,
                null,
                items);
    }

    public static SalesOrder createB2C(
            Long orderNumber,
            FulfillmentType fulfillmentType,
            PaymentMethod paymentMethod,
            DeliveryRoute deliveryRoute,
            UUID sellerCompanyId,
            UUID customerId,
            Set<SalesOrderItem> items) {
        return create(
                orderNumber,
                SaleType.B2C,
                fulfillmentType,
                paymentMethod,
                deliveryRoute,
                sellerCompanyId,
                null,
                customerId,
                items);
    }

    private static SalesOrder create(
            Long orderNumber,
            SaleType saleType,
            FulfillmentType fulfillmentType,
            PaymentMethod paymentMethod,
            DeliveryRoute deliveryRoute,
            UUID sellerCompanyId,
            UUID buyerCompanyId,
            UUID customerId,
            Set<SalesOrderItem> items) {
        DomainAssert.requireNonNull(orderNumber, SalesOrderErrorCode.NUMBER_REQUIRED);
        DomainAssert.requireNonNull(saleType, SalesOrderErrorCode.SALE_TYPE_REQUIRED);
        DomainAssert.requireNonNull(fulfillmentType, SalesOrderErrorCode.FULFILLMENT_TYPE_REQUIRED);
        DomainAssert.requireNonNull(paymentMethod, SalesOrderErrorCode.PAYMENT_METHOD_REQUIRED);
        DomainAssert.requireNonNull(sellerCompanyId, SalesOrderErrorCode.SELLER_COMPANY_REQUIRED);
        validateSaleType(saleType, buyerCompanyId, customerId);

        if (items == null || items.isEmpty()) {
            throw new SalesOrderException(SalesOrderErrorCode.ITEMS_REQUIRED);
        }

        validateFulfillment(fulfillmentType, deliveryRoute, items);
        validateSingleCurrency(items);

        SalesOrder order = new SalesOrder(
                orderNumber,
                saleType,
                fulfillmentType,
                paymentMethod,
                deliveryRoute,
                sellerCompanyId,
                buyerCompanyId,
                customerId);

        items.forEach(order::addItem);

        return order;
    }

    private static void validateSaleType(SaleType saleType, UUID buyerCompanyId, UUID customerId) {
        if (saleType == SaleType.B2B) {
            DomainAssert.requireNonNull(buyerCompanyId, SalesOrderErrorCode.BUYER_COMPANY_REQUIRED_FOR_B2B);

            if (customerId != null) {
                throw new SalesOrderException(SalesOrderErrorCode.CUSTOMER_NOT_ALLOWED_FOR_B2B);
            }
        }

        if (saleType == SaleType.B2C) {
            DomainAssert.requireNonNull(customerId, SalesOrderErrorCode.CUSTOMER_REQUIRED_FOR_B2C);

            if (buyerCompanyId != null) {
                throw new SalesOrderException(SalesOrderErrorCode.BUYER_COMPANY_NOT_ALLOWED_FOR_B2C);
            }
        }
    }

    private static void validateFulfillment(
            FulfillmentType fulfillmentType, DeliveryRoute deliveryRoute, Set<SalesOrderItem> items) {
        if (fulfillmentType == FulfillmentType.DELIVERY && deliveryRoute == null) {
            throw new SalesOrderException(DeliveryRouteErrorCode.REQUIRED_FOR_DELIVERY);
        }

        if (fulfillmentType == FulfillmentType.PICKUP && deliveryRoute != null) {
            throw new SalesOrderException(DeliveryRouteErrorCode.NOT_ALLOWED_FOR_PICKUP);
        }

        if (fulfillmentType == FulfillmentType.DELIVERY) {
            boolean hasDifferentWarehouse = items.stream()
                    .anyMatch(item -> !item.getWarehouseId().equals(deliveryRoute.getOriginWarehouseId()));

            if (hasDifferentWarehouse) {
                throw new SalesOrderException(DeliveryRouteErrorCode.ITEMS_MUST_USE_SINGLE_ORIGIN_WAREHOUSE);
            }
        }
    }

    private static void validateSingleCurrency(Set<SalesOrderItem> items) {
        long currencyCount =
                items.stream().map(SalesOrderItem::getCurrencyCode).distinct().count();

        if (currencyCount > 1) {
            throw new SalesOrderException(SalesOrderErrorCode.ITEMS_MUST_USE_SINGLE_CURRENCY);
        }
    }

    public void confirm() {
        if (this.status != OrderStatus.DRAFT) {
            throw new SalesOrderException(SalesOrderErrorCode.MUST_BE_DRAFT_TO_CONFIRM);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            return;
        }
        this.status = OrderStatus.CANCELLED;
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
        return items.iterator().next().getCurrencyCode();
    }

    public SaleType getSaleType() {
        return saleType;
    }

    public FulfillmentType getFulfillmentType() {
        return fulfillmentType;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
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

    public Set<SalesOrderItem> getItems() {
        return Collections.unmodifiableSet(items);
    }

    private void addItem(SalesOrderItem item) {
        SalesOrderItem requiredItem = DomainAssert.requireNonNull(item, SalesOrderErrorCode.ITEM_REQUIRED);
        requiredItem.assignTo(this);
        this.items.add(requiredItem);
    }

    void removeItem(SalesOrderItem item) {
        if (this.status != OrderStatus.DRAFT) {
            throw new SalesOrderException(SalesOrderErrorCode.MUST_BE_DRAFT_TO_REMOVE_ITEMS);
        }

        SalesOrderItem requiredItem = DomainAssert.requireNonNull(item, SalesOrderErrorCode.ITEM_REQUIRED);
        if (this.items.remove(requiredItem)) {
            requiredItem.unassignFrom(this);
        }
    }
}
