package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.financial.internal.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findBySalesOrderId(UUID salesOrderId);

    Optional<Invoice> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<Invoice> findBySalesOrderId(UUID salesOrderId, Pageable pageable);

    Page<Invoice> findAllByCompanyId(UUID companyId, Pageable pageable);

    Page<Invoice> findByCompanyIdAndSalesOrderId(UUID companyId, UUID salesOrderId, Pageable pageable);

    boolean existsBySalesOrderId(UUID salesOrderId);

    boolean existsByCompanyIdAndSalesOrderId(UUID companyId, UUID salesOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select invoice from Invoice invoice where invoice.id = :id")
    Optional<Invoice> findWithLockingById(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select invoice from Invoice invoice where invoice.id = :id and invoice.companyId = :companyId")
    Optional<Invoice> findWithLockingByIdAndCompanyId(@Param("id") UUID id, @Param("companyId") UUID companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select invoice from Invoice invoice where invoice.salesOrderId = :salesOrderId")
    Optional<Invoice> findWithLockingBySalesOrderId(@Param("salesOrderId") UUID salesOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select invoice from Invoice invoice where invoice.salesOrderId = :salesOrderId and invoice.companyId = :companyId")
    Optional<Invoice> findWithLockingByCompanyIdAndSalesOrderId(
            @Param("companyId") UUID companyId, @Param("salesOrderId") UUID salesOrderId);
}
