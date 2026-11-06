package com.thiagsilvadev.erpapi.logistics.internal.model;

import java.time.Instant;
import java.util.*;

import jakarta.persistence.*;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;

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
        Long requiredShipmentNumber = DomainAssert.requireNotNull(shipmentNumber, "Shipment number");
        UUID requiredSalesOrderId = DomainAssert.requireNotNull(salesOrderId, "Sales order");
        UUID requiredSellerCompanyId = DomainAssert.requireNotNull(sellerCompanyId, "Seller company");
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
        if (status != ShipmentStatus.CREATED) {
            throw new ConflictException("Cannot assign route to shipment in status " + status);
        }
        this.routeName = DomainAssert.requireNotNull(routeName, "Route name");
        this.originWarehouseId = DomainAssert.requireNotNull(originWarehouseId, "Origin warehouse");
        this.destinationDescription = DomainAssert.requireNotNull(destinationDescription, "Destination description");
    }

    public void assignDispatch(
            Driver driver, Vehicle vehicle, String routeName, UUID originWarehouseId, String destinationDescription) {
        status.requireTransitionTo(ShipmentStatus.DISPATCH_ASSIGNED);
        this.driver = DomainAssert.requireNotNull(driver, "Driver");
        this.vehicle = DomainAssert.requireNotNull(vehicle, "Vehicle");

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
        status.requireTransitionTo(ShipmentStatus.LOADED);
        this.loadedAt = DomainAssert.requireNotNull(loadedAt, "Loaded timestamp");
        this.status = ShipmentStatus.LOADED;
    }

    public void dispatch(Instant dispatchedAt) {
        status.requireTransitionTo(ShipmentStatus.IN_TRANSIT);
        this.dispatchedAt = DomainAssert.requireNotNull(dispatchedAt, "Dispatched timestamp");
        this.status = ShipmentStatus.IN_TRANSIT;
    }

    public void deliver(Instant deliveredAt) {
        status.requireTransitionTo(ShipmentStatus.DELIVERED);
        this.deliveredAt = DomainAssert.requireNotNull(deliveredAt, "Delivered timestamp");
        this.status = ShipmentStatus.DELIVERED;
    }

    public void cancel(Instant cancelledAt) {
        status.requireTransitionTo(ShipmentStatus.CANCELLED);
        this.cancelledAt = DomainAssert.requireNotNull(cancelledAt, "Cancelled timestamp");
        this.status = ShipmentStatus.CANCELLED;
    }

    public void ensureBeforeLoading() {
        status.requireTransitionTo(ShipmentStatus.CANCELLED);
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
