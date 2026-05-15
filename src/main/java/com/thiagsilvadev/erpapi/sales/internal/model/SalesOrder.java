package com.thiagsilvadev.erpapi.sales.internal.model;

import java.math.BigDecimal;
import java.util.*;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.sales.api.OrderStatus;

@Entity
@Table(name = "sales_orders")
public class SalesOrder extends AbstractBaseEntity {

    @Column(nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.DRAFT;

    @ElementCollection
    @CollectionTable(name = "sales_order_items", joinColumns = @JoinColumn(name = "sales_order_id"))
    private List<SalesOrderItem> items = new ArrayList<>();

    protected SalesOrder() {}

    private SalesOrder(UUID customerId, List<SalesOrderItem> items) {
        this.customerId = Objects.requireNonNull(customerId, "Customer is required");
        this.items.addAll(items);
    }

    public static SalesOrder create(UUID customerId, List<SalesOrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Sales order must have at least one item");
        }
        return new SalesOrder(customerId, items);
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

    public UUID getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<SalesOrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
