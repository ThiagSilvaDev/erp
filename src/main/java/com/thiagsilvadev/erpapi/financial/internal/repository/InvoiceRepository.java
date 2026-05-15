package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {}
