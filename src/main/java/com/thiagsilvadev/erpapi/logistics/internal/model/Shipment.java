package com.thiagsilvadev.erpapi.logistics.internal.model;

import java.time.Instant;
import java.util.*;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;

@Entity
@Table(
        name = "logistics_shipments",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_logistics_shipment_seller_number",
                    columnNames = {"seller_company_id", "shipment_number"}),
            @UniqueConstraint(
                    name = "uk_logistics_shipment_seller_sales_order",
                    columnNames = {"seller_company_id", "sales_order_id"})
        })
public class Shipment extends AbstractBaseEntity {

    @Column(name = "shipment_number", nullable = false, updatable = false)
    private Long shipmentNumber;

    @Column(name = "sales_order_id", nullable = false, updatable = false)
    private UUID salesOrderId;

    @Column(nullable = false)
    private UUID sellerCompanyId;

    @Column
    private UUID buyerCompanyId;

    @Column
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status = ShipmentStatus.CREATED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column
    private String routeName;

    @Column
    private UUID originWarehouseId;

    @Column
    private String destinationDescription;

    @Column
    private Instant loadedAt;

    @Column
    private Instant dispatchedAt;

    @Column
    private Instant deliveredAt;

    @Column
    private Instant cancelledAt;

    @ElementCollection
    @CollectionTable(name = "logistics_shipment_items", joinColumns = @JoinColumn(name = "shipment_id"))
    private List<ShipmentItem> items = new ArrayList<>();

    protected Shipment() {}

    private Shipment(
            Long shipmentNumber,
            UUID salesOrderId,
            UUID sellerCompanyId,
            UUID buyerCompanyId,
            UUID customerId,
            List<ShipmentItem> items) {
        this.shipmentNumber = shipmentNumber;
        this.salesOrderId = salesOrderId;
        this.sellerCompanyId = sellerCompanyId;
        this.buyerCompanyId = buyerCompanyId;
        this.customerId = customerId;
        this.items.addAll(items);
    }

    public static Shipment create(
            Long shipmentNumber,
            UUID salesOrderId,
            UUID sellerCompanyId,
            UUID buyerCompanyId,
            UUID customerId,
            List<ShipmentItem> items) {
        Long requiredShipmentNumber = Objects.requireNonNull(shipmentNumber, "Shipment number is required");
        UUID requiredSalesOrderId = Objects.requireNonNull(salesOrderId, "Sales order is required");
        UUID requiredSellerCompanyId = Objects.requireNonNull(sellerCompanyId, "Seller company is required");
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Shipment must have at least one item");
        }

        return new Shipment(
                requiredShipmentNumber,
                requiredSalesOrderId,
                requiredSellerCompanyId,
                buyerCompanyId,
                customerId,
                List.copyOf(items));
    }

    public void assignRoute(String routeName, UUID originWarehouseId, String destinationDescription) {
        requireStatus(ShipmentStatus.CREATED);
        this.routeName = Objects.requireNonNull(routeName, "Route name is required");
        this.originWarehouseId = Objects.requireNonNull(originWarehouseId, "Origin warehouse is required");
        this.destinationDescription =
                Objects.requireNonNull(destinationDescription, "Destination description is required");
    }

    public void assignDispatch(
            Driver driver, Vehicle vehicle, String routeName, UUID originWarehouseId, String destinationDescription) {
        requireStatus(ShipmentStatus.CREATED, ShipmentStatus.DISPATCH_ASSIGNED);
        this.driver = Objects.requireNonNull(driver, "Driver is required");
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle is required");

        if (routeName != null) {
            this.routeName = routeName;
        }
        if (originWarehouseId != null) {
            this.originWarehouseId = originWarehouseId;
        }
        if (destinationDescription != null) {
            this.destinationDescription = destinationDescription;
        }

        if (this.routeName == null || this.routeName.isBlank()) {
            throw new IllegalArgumentException("Route name is required");
        }
        if (this.originWarehouseId == null) {
            throw new IllegalArgumentException("Origin warehouse is required");
        }
        if (this.destinationDescription == null || this.destinationDescription.isBlank()) {
            throw new IllegalArgumentException("Destination description is required");
        }

        this.status = ShipmentStatus.DISPATCH_ASSIGNED;
    }

    public void load(Instant loadedAt) {
        requireStatus(ShipmentStatus.DISPATCH_ASSIGNED);
        this.loadedAt = Objects.requireNonNull(loadedAt, "Loaded timestamp is required");
        this.status = ShipmentStatus.LOADED;
    }

    public void dispatch(Instant dispatchedAt) {
        requireStatus(ShipmentStatus.LOADED);
        this.dispatchedAt = Objects.requireNonNull(dispatchedAt, "Dispatched timestamp is required");
        this.status = ShipmentStatus.IN_TRANSIT;
    }

    public void deliver(Instant deliveredAt) {
        requireStatus(ShipmentStatus.IN_TRANSIT);
        this.deliveredAt = Objects.requireNonNull(deliveredAt, "Delivered timestamp is required");
        this.status = ShipmentStatus.DELIVERED;
    }

    public void cancel(Instant cancelledAt) {
        requireStatus(ShipmentStatus.CREATED, ShipmentStatus.DISPATCH_ASSIGNED);
        this.cancelledAt = Objects.requireNonNull(cancelledAt, "Cancelled timestamp is required");
        this.status = ShipmentStatus.CANCELLED;
    }

    public void ensureBeforeLoading() {
        requireStatus(ShipmentStatus.CREATED, ShipmentStatus.DISPATCH_ASSIGNED);
    }

    private void requireStatus(ShipmentStatus... allowed) {
        if (!Set.of(allowed).contains(status)) {
            throw new IllegalStateException("Invalid shipment status transition from " + status);
        }
    }

    public Long getShipmentNumber() {
        return shipmentNumber;
    }

    public String getFormattedShipmentNumber() {
        return "SHP-%06d".formatted(shipmentNumber);
    }

    public UUID getSalesOrderId() {
        return salesOrderId;
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

    public ShipmentStatus getStatus() {
        return status;
    }

    public Driver getDriver() {
        return driver;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getRouteName() {
        return routeName;
    }

    public UUID getOriginWarehouseId() {
        return originWarehouseId;
    }

    public String getDestinationDescription() {
        return destinationDescription;
    }

    public Instant getLoadedAt() {
        return loadedAt;
    }

    public Instant getDispatchedAt() {
        return dispatchedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public List<ShipmentItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
