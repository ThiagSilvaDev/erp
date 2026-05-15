package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.financial.internal.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {}
