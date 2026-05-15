package com.thiagsilvadev.erpapi.inventory.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {}
