package com.thiagsilvadev.erpapi.logistics.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

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
                .isInstanceOf(IllegalStateException.class);
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
                .isInstanceOf(IllegalStateException.class);
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
