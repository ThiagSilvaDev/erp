package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.util.UUID;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.financial.internal.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    boolean existsByInvoiceId(UUID invoiceId);

    @Query("select coalesce(sum(payment.amount), 0) from Payment payment where payment.invoiceId = :invoiceId")
    BigDecimal sumAmountByInvoiceId(@Param("invoiceId") UUID invoiceId);
}
