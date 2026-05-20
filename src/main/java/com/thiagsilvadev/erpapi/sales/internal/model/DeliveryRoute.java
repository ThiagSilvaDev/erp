package com.thiagsilvadev.erpapi.sales.internal.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DeliveryRoute {

    @Column(name = "delivery_route_name")
    private String routeName;

    @Column(name = "delivery_origin_warehouse_id")
    private UUID originWarehouseId;

    @Column(name = "delivery_destination_description")
    private String destinationDescription;

    protected DeliveryRoute() {}

    private DeliveryRoute(String routeName, UUID originWarehouseId, String destinationDescription) {
        this.routeName = routeName;
        this.originWarehouseId = originWarehouseId;
        this.destinationDescription = destinationDescription;
    }

    public static DeliveryRoute create(String routeName, UUID originWarehouseId, String destinationDescription) {
        return new DeliveryRoute(routeName, originWarehouseId, destinationDescription);
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
}
