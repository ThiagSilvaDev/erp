package com.thiagsilvadev.erpapi.financial.internal.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.financial.internal.model.AccountingEntry;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingEntrySourceType;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingEntryStatus;

public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, UUID> {

    Page<AccountingEntry> findAllByCompanyId(UUID companyId, Pageable pageable);

    Page<AccountingEntry> findByCompanyIdAndAccountId(UUID companyId, UUID accountId, Pageable pageable);

    Page<AccountingEntry> findByCompanyIdAndCompetenceDateBetween(
            UUID companyId, LocalDate from, LocalDate to, Pageable pageable);

    Page<AccountingEntry> findByCompanyIdAndAccountIdAndCompetenceDateBetween(
            UUID companyId, UUID accountId, LocalDate from, LocalDate to, Pageable pageable);

    List<AccountingEntry> findByCompanyIdAndSourceTypeAndSourceIdAndStatus(
            UUID companyId, AccountingEntrySourceType sourceType, UUID sourceId, AccountingEntryStatus status);

    long countByCompanyIdAndSourceType(UUID companyId, AccountingEntrySourceType sourceType);

    @Modifying(flushAutomatically = true)
    @Query(
            value = "update financial_accounting_entries set competence_date = :competenceDate where id = :id",
            nativeQuery = true)
    void updateCompetenceDateForDevSeed(@Param("id") UUID id, @Param("competenceDate") LocalDate competenceDate);

    @Query(
            value =
                    """
                    select a.id as "accountId",
                           a.code as "accountCode",
                           a.name as "accountName",
                           a.type as "accountType",
                           e.currency_code as "currencyCode",
                           coalesce(sum(
                             case
                               when a.type = 'REVENUE' and e.direction = 'CREDIT' then e.amount
                               when a.type = 'REVENUE' and e.direction = 'DEBIT' then -e.amount
                               when a.type = 'EXPENSE' and e.direction = 'DEBIT' then e.amount
                               when a.type = 'EXPENSE' and e.direction = 'CREDIT' then -e.amount
                               when a.type in ('ASSET', 'LIABILITY', 'EQUITY') and e.direction = 'DEBIT'
                                 then e.amount
                               when a.type in ('ASSET', 'LIABILITY', 'EQUITY') and e.direction = 'CREDIT'
                                 then -e.amount
                               else 0
                             end
                           ), 0) as "amount"
                      from financial_accounting_entries e
                      join financial_accounting_accounts a on a.id = e.account_id
                     where e.company_id = :companyId
                       and e.status = 'POSTED'
                       and e.competence_date >= :fromDate
                       and e.competence_date <= :toDate
                       and e.currency_code = :currencyCode
                     group by a.id, a.code, a.name, a.type, e.currency_code
                     order by a.code
                    """,
            nativeQuery = true)
    List<AccountBalanceRow> findAccountBalances(
            @Param("companyId") UUID companyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("currencyCode") String currencyCode);

    interface AccountBalanceRow {
        UUID getAccountId();

        String getAccountCode();

        String getAccountName();

        String getAccountType();

        String getCurrencyCode();

        BigDecimal getAmount();
    }
}
