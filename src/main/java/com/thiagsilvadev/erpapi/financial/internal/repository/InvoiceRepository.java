package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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

    @Query(
            value =
                    """
                    select (invoice.created_at at time zone 'UTC')::date as "date",
                           invoice.currency_code as "currencyCode",
                           count(*) as "count",
                           coalesce(sum(invoice.amount), 0) as "amount"
                      from financial_invoices invoice
                     where invoice.company_id = :companyId
                       and invoice.status <> 'CANCELLED'
                       and invoice.created_at >= :fromInclusive
                       and invoice.created_at < :toExclusive
                     group by (invoice.created_at at time zone 'UTC')::date, invoice.currency_code
                     order by 1, 2
                    """,
            nativeQuery = true)
    List<InvoiceKpiRow> findKpiRows(
            @Param("companyId") UUID companyId,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive);

    @Query(
            value =
                    """
                    select invoice.currency_code as "currencyCode",
                           coalesce(sum(greatest(invoice.amount - coalesce(paid.amount, 0), 0)), 0) as "openAmount",
                           coalesce(sum(case when invoice.due_date < :asOf
                                then greatest(invoice.amount - coalesce(paid.amount, 0), 0) else 0 end), 0)
                                as "overdueAmount"
                      from financial_invoices invoice
                      left join (
                          select payment.invoice_id, sum(payment.amount) as amount
                            from financial_payments payment
                           where payment.company_id = :companyId
                             and payment.status = 'PAID'
                           group by payment.invoice_id
                      ) paid on paid.invoice_id = invoice.id
                     where invoice.company_id = :companyId
                       and invoice.status = 'OPEN'
                     group by invoice.currency_code
                     order by invoice.currency_code
                    """,
            nativeQuery = true)
    List<ReceivableKpiRow> findReceivableKpiRows(@Param("companyId") UUID companyId, @Param("asOf") LocalDate asOf);

    interface InvoiceKpiRow {
        LocalDate getDate();

        String getCurrencyCode();

        long getCount();

        BigDecimal getAmount();
    }

    interface ReceivableKpiRow {
        String getCurrencyCode();

        BigDecimal getOpenAmount();

        BigDecimal getOverdueAmount();
    }
}
