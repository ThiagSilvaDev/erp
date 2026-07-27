package com.thiagsilvadev.erpapi.inventory.internal.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.inventory.internal.model.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    Optional<StockMovement> findByIdAndCompanyId(UUID id, UUID companyId);

    Page<StockMovement> findAllByCompanyId(UUID companyId, Pageable pageable);

    long countByCompanyId(UUID companyId);

    @Modifying(flushAutomatically = true)
    @Query(value = "update inventory_stock_movements set created_at = :createdAt where id = :id", nativeQuery = true)
    void updateCreatedAtForDevSeed(@Param("id") UUID id, @Param("createdAt") Instant createdAt);

    @Query(
            value =
                    """
                    select (movement.created_at at time zone 'UTC')::date as "date",
                           coalesce(sum(case when movement.type in ('IN', 'TRANSFER_IN')
                                then movement.quantity else 0 end), 0) as "inbound",
                           coalesce(sum(case when movement.type in ('OUT', 'TRANSFER_OUT')
                                then movement.quantity else 0 end), 0) as "outbound",
                           coalesce(sum(case when movement.type = 'RESERVE'
                                then movement.quantity else 0 end), 0) as "reserved",
                           coalesce(sum(case when movement.type = 'RELEASE_RESERVATION'
                                then movement.quantity else 0 end), 0) as "released",
                           coalesce(sum(case when movement.type = 'ADJUSTMENT'
                                then movement.quantity else 0 end), 0) as "adjusted"
                      from inventory_stock_movements movement
                     where movement.company_id = :companyId
                       and movement.created_at >= :fromInclusive
                       and movement.created_at < :toExclusive
                     group by (movement.created_at at time zone 'UTC')::date
                     order by 1
                    """,
            nativeQuery = true)
    List<MovementKpiRow> findKpiRows(
            @Param("companyId") UUID companyId,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive);

    interface MovementKpiRow {
        LocalDate getDate();

        long getInbound();

        long getOutbound();

        long getReserved();

        long getReleased();

        long getAdjusted();
    }
}
