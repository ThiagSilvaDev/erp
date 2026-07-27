package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.financial.internal.model.FinancialEntry;

public interface FinancialEntryRepository extends JpaRepository<FinancialEntry, UUID> {

    Page<FinancialEntry> findByAccountId(UUID accountId, Pageable pageable);

    Page<FinancialEntry> findAllByCompanyId(UUID companyId, Pageable pageable);

    Page<FinancialEntry> findByCompanyIdAndAccountId(UUID companyId, UUID accountId, Pageable pageable);

    @Query(
            value =
                    """
                    select (entry.occurred_at at time zone 'UTC')::date as "date",
                           entry.currency_code as "currencyCode",
                           coalesce(sum(entry.amount), 0) as "amount"
                      from financial_entries entry
                     where entry.company_id = :companyId
                       and entry.type = 'INCOME'
                       and entry.source = 'PAYMENT'
                       and entry.occurred_at >= :fromInclusive
                       and entry.occurred_at < :toExclusive
                     group by (entry.occurred_at at time zone 'UTC')::date, entry.currency_code
                     order by 1, 2
                    """,
            nativeQuery = true)
    List<CollectionKpiRow> findCollectionKpiRows(
            @Param("companyId") UUID companyId,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive);

    interface CollectionKpiRow {
        LocalDate getDate();

        String getCurrencyCode();

        BigDecimal getAmount();
    }
}
