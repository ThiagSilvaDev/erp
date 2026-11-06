package com.thiagsilvadev.erpapi.logistics.internal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.logistics.internal.model.ShipmentTrackingEvent;

public interface ShipmentTrackingEventRepository extends JpaRepository<ShipmentTrackingEvent, UUID> {

    List<ShipmentTrackingEvent> findByShipmentIdOrderByOccurredAtAsc(UUID shipmentId);
}
