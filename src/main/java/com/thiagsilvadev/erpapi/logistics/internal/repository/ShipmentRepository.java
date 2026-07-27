package com.thiagsilvadev.erpapi.logistics.internal.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.logistics.internal.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID>, JpaSpecificationExecutor<Shipment> {

    boolean existsBySalesOrderId(UUID salesOrderId);

    boolean existsBySellerCompanyIdAndSalesOrderId(UUID sellerCompanyId, UUID salesOrderId);

    Optional<Shipment> findBySalesOrderId(UUID salesOrderId);

    Optional<Shipment> findBySellerCompanyIdAndSalesOrderId(UUID sellerCompanyId, UUID salesOrderId);

    Optional<Shipment> findByIdAndSellerCompanyId(UUID id, UUID sellerCompanyId);

    @Query(
            value =
                    """
                    select count(*) filter (where shipment.status = 'CREATED') as "created",
                           count(*) filter (where shipment.status = 'DISPATCH_ASSIGNED') as "dispatchAssigned",
                           count(*) filter (where shipment.status = 'LOADED') as "loaded",
                           count(*) filter (where shipment.status = 'IN_TRANSIT') as "inTransit"
                      from logistics_shipments shipment
                     where shipment.seller_company_id = :companyId
                    """,
            nativeQuery = true)
    CurrentKpiRow findCurrentKpiRow(@Param("companyId") UUID companyId);

    @Query(
            value =
                    """
                    select event_date as "date",
                           sum(created) as "created",
                           sum(delivered) as "delivered",
                           sum(cancelled) as "cancelled",
                           coalesce(sum(delivery_hours), 0) as "deliveryHoursTotal",
                           sum(deliveries_with_duration) as "deliveriesWithDuration"
                      from (
                          select (shipment.created_at at time zone 'UTC')::date as event_date,
                                 1 as created, 0 as delivered, 0 as cancelled,
                                 0::numeric as delivery_hours, 0 as deliveries_with_duration
                            from logistics_shipments shipment
                           where shipment.seller_company_id = :companyId
                             and shipment.created_at >= :fromInclusive
                             and shipment.created_at < :toExclusive
                          union all
                          select (shipment.delivered_at at time zone 'UTC')::date,
                                 0, 1, 0,
                                 extract(epoch from (shipment.delivered_at - shipment.dispatched_at)) / 3600,
                                 case when shipment.dispatched_at is null then 0 else 1 end
                            from logistics_shipments shipment
                           where shipment.seller_company_id = :companyId
                             and shipment.delivered_at >= :fromInclusive
                             and shipment.delivered_at < :toExclusive
                          union all
                          select (shipment.cancelled_at at time zone 'UTC')::date,
                                 0, 0, 1, 0::numeric, 0
                            from logistics_shipments shipment
                           where shipment.seller_company_id = :companyId
                             and shipment.cancelled_at >= :fromInclusive
                             and shipment.cancelled_at < :toExclusive
                      ) events
                     group by event_date
                     order by event_date
                    """,
            nativeQuery = true)
    List<DailyKpiRow> findDailyKpiRows(
            @Param("companyId") UUID companyId,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive);

    interface CurrentKpiRow {
        long getCreated();

        long getDispatchAssigned();

        long getLoaded();

        long getInTransit();
    }

    interface DailyKpiRow {
        LocalDate getDate();

        long getCreated();

        long getDelivered();

        long getCancelled();

        BigDecimal getDeliveryHoursTotal();

        long getDeliveriesWithDuration();
    }
}
