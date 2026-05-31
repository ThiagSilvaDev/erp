package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.financial.internal.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    boolean existsByInvoiceId(UUID invoiceId);

    boolean existsByCompanyIdAndInvoiceId(UUID companyId, UUID invoiceId);

    Page<Payment> findAllByCompanyId(UUID companyId, Pageable pageable);

    java.util.Optional<Payment> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("select coalesce(sum(payment.amount), 0) from Payment payment where payment.invoiceId = :invoiceId")
    BigDecimal sumAmountByInvoiceId(@Param("invoiceId") UUID invoiceId);

    @Query(
            "select coalesce(sum(payment.amount), 0) from Payment payment where payment.companyId = :companyId and payment.invoiceId = :invoiceId")
    BigDecimal sumAmountByCompanyIdAndInvoiceId(@Param("companyId") UUID companyId, @Param("invoiceId") UUID invoiceId);
}
