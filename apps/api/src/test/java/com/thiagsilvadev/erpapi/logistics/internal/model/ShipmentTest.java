package com.thiagsilvadev.erpapi.logistics.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.InvalidAttributeException;

class ShipmentTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();

    @Test
    void shouldFollowShipmentWorkflow() {
        Shipment shipment = shipment();
        Driver driver = Driver.create(COMPANY_ID, UUID.randomUUID(), "CNH-1");
        Vehicle vehicle = Vehicle.create(COMPANY_ID, "ABC1D23", "Truck", 1000);

        shipment.assignDispatch(driver, vehicle, "Downtown", UUID.randomUUID(), "Customer dock");
        shipment.load(Instant.parse("2026-01-01T10:00:00Z"));
        shipment.dispatch(Instant.parse("2026-01-01T11:00:00Z"));
        shipment.deliver(Instant.parse("2026-01-01T12:00:00Z"));

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
    }

    @Test
    void shouldRejectLoadingBeforeDispatchAssignment() {
        Shipment shipment = shipment();

        assertThatThrownBy(() -> shipment.load(Instant.parse("2026-01-01T10:00:00Z")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldRejectCancellationAfterLoading() {
        Shipment shipment = shipment();
        shipment.assignDispatch(
                Driver.create(COMPANY_ID, UUID.randomUUID(), "CNH-1"),
                Vehicle.create(COMPANY_ID, "ABC1D23", "Truck", 1000),
                "Downtown",
                UUID.randomUUID(),
                "Customer dock");
        shipment.load(Instant.parse("2026-01-01T10:00:00Z"));

        assertThatThrownBy(() -> shipment.cancel(Instant.parse("2026-01-01T11:00:00Z")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldPreserveFieldsWhenTransitionsAreRejected() {
        Shipment shipment = shipment();
        Instant loadedAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant attemptedAt = loadedAt.plusSeconds(3600);
        Driver driver = Driver.create(COMPANY_ID, UUID.randomUUID(), "CNH-1");
        Vehicle vehicle = Vehicle.create(COMPANY_ID, "ABC1D23", "Truck", 1000);
        UUID warehouseId = UUID.randomUUID();
        shipment.assignDispatch(driver, vehicle, "Downtown", warehouseId, "Customer dock");
        shipment.load(loadedAt);

        assertThatThrownBy(() -> shipment.load(attemptedAt)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> shipment.deliver(attemptedAt)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> shipment.cancel(attemptedAt)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> shipment.assignDispatch(null, null, "Changed", null, "Changed"))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> shipment.assignRoute("Changed", warehouseId, "Changed"))
                .isInstanceOf(ConflictException.class);

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.LOADED);
        assertThat(shipment.getLoadedAt()).isEqualTo(loadedAt);
        assertThat(shipment.getDispatchedAt()).isNull();
        assertThat(shipment.getDeliveredAt()).isNull();
        assertThat(shipment.getCancelledAt()).isNull();
        assertThat(shipment.getDriver()).isSameAs(driver);
        assertThat(shipment.getVehicle()).isSameAs(vehicle);
        assertThat(shipment.getRouteName()).isEqualTo("Downtown");
        assertThat(shipment.getOriginWarehouseId()).isEqualTo(warehouseId);
        assertThat(shipment.getDestinationDescription()).isEqualTo("Customer dock");
    }

    @Test
    void shouldReassignDispatchBeforeLoading() {
        Shipment shipment = shipment();
        Driver firstDriver = Driver.create(COMPANY_ID, UUID.randomUUID(), "CNH-1");
        Driver nextDriver = Driver.create(COMPANY_ID, UUID.randomUUID(), "CNH-2");
        Vehicle vehicle = Vehicle.create(COMPANY_ID, "ABC1D23", "Truck", 1000);
        shipment.assignDispatch(firstDriver, vehicle, "Downtown", UUID.randomUUID(), "Customer dock");

        shipment.assignDispatch(nextDriver, vehicle, null, null, null);

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DISPATCH_ASSIGNED);
        assertThat(shipment.getDriver()).isSameAs(nextDriver);
        assertThat(shipment.getRouteName()).isEqualTo("Downtown");
    }

    @Test
    void shouldPreserveStatusAndTimestampWhenLoadingInputIsInvalid() {
        Shipment shipment = shipment();
        shipment.assignDispatch(
                Driver.create(COMPANY_ID, UUID.randomUUID(), "CNH-1"),
                Vehicle.create(COMPANY_ID, "ABC1D23", "Truck", 1000),
                "Downtown",
                UUID.randomUUID(),
                "Customer dock");

        assertThatThrownBy(() -> shipment.load(null)).isInstanceOf(InvalidAttributeException.class);

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DISPATCH_ASSIGNED);
        assertThat(shipment.getLoadedAt()).isNull();
    }

    private Shipment shipment() {
        return Shipment.create(
                1L,
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                List.of(ShipmentItem.create(UUID.randomUUID(), UUID.randomUUID(), "SKU-1", "Product", 2)));
    }
}
