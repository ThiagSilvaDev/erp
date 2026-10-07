package com.thiagsilvadev.erpapi.sales.internal.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.sales.internal.model.SalesOrder;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID>, JpaSpecificationExecutor<SalesOrder> {

    Optional<SalesOrder> findByIdAndSellerCompanyId(UUID id, UUID sellerCompanyId);

    List<SalesOrder> findAllBySellerCompanyId(UUID sellerCompanyId);

    long countBySellerCompanyId(UUID sellerCompanyId);

    @Modifying(flushAutomatically = true)
    @Query(value = "update sales_orders set created_at = :createdAt where id = :id", nativeQuery = true)
    void updateCreatedAtForDevSeed(@Param("id") UUID id, @Param("createdAt") Instant createdAt);

    @Query(
            value =
                    """
                    select (orders.created_at at time zone 'UTC')::date as "date",
                           items.currency_code as "currencyCode",
                           count(distinct orders.id) as "orders",
                           count(distinct orders.id) filter (where orders.status = 'CONFIRMED') as "confirmedOrders",
                           count(distinct orders.id) filter (where orders.status = 'CANCELLED') as "cancelledOrders",
                           coalesce(sum(case when orders.status = 'CONFIRMED'
                                then items.unit_price_amount * items.quantity else 0 end), 0) as "confirmedRevenue"
                      from sales_orders orders
                      join sales_order_items items on items.sales_order_id = orders.id
                     where orders.seller_company_id = :companyId
                       and orders.created_at >= :fromInclusive
                       and orders.created_at < :toExclusive
                     group by (orders.created_at at time zone 'UTC')::date, items.currency_code
                     order by 1, 2
                    """,
            nativeQuery = true)
    List<SalesKpiRow> findKpiRows(
            @Param("companyId") UUID companyId,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive);

    interface SalesKpiRow {
        LocalDate getDate();

        String getCurrencyCode();

        long getOrders();

        long getConfirmedOrders();

        long getCancelledOrders();

        BigDecimal getConfirmedRevenue();
    }
}
