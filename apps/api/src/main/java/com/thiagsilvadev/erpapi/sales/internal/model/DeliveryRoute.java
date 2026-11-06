package com.thiagsilvadev.erpapi.sales.internal.model;

import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import com.thiagsilvadev.erpapi.common.domain.model.GeoLocation;
import com.thiagsilvadev.erpapi.common.domain.validation.DomainAssert;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;

@Embeddable
public class DeliveryRoute {

    @Column(name = "route_name", length = 120)
    private String routeName;

    @Column(name = "origin_warehouse_id")
    private UUID originWarehouseId;

    @Column(name = "destination_description", length = 500)
    private String destinationDescription;

    @Embedded
    @AttributeOverride(name = "latitude", column = @Column(name = "destination_latitude", precision = 10, scale = 7))
    @AttributeOverride(name = "longitude", column = @Column(name = "destination_longitude", precision = 10, scale = 7))
    private GeoLocation destinationLocation;

    protected DeliveryRoute() {}

    private DeliveryRoute(
            String routeName, UUID originWarehouseId, String destinationDescription, GeoLocation destinationLocation) {
        this.routeName = routeName;
        this.originWarehouseId = originWarehouseId;
        this.destinationDescription = destinationDescription;
        this.destinationLocation = destinationLocation;
    }

    public static DeliveryRoute of(
            String routeName, UUID originWarehouseId, String destinationDescription, GeoLocation destinationLocation) {
        return new DeliveryRoute(
                Strings.required(routeName, "Route name"),
                DomainAssert.requireNotNull(originWarehouseId, "Origin warehouse ID"),
                Strings.required(destinationDescription, "Destination description"),
                destinationLocation);
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

    public GeoLocation getDestinationLocation() {
        return destinationLocation;
    }
}
