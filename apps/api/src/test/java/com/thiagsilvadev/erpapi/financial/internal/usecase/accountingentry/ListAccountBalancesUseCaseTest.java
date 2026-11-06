package com.thiagsilvadev.erpapi.financial.internal.usecase.accountingentry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountBalanceCriteria;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingentry.AccountBalanceResponse;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccountType;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingEntryRepository;

@ExtendWith(MockitoExtension.class)
class ListAccountBalancesUseCaseTest {
    private static final UUID TENANT_ID = UUID.randomUUID();

    @Mock
    private AccountingEntryRepository entryRepository;

    @Mock
    private AccountingEntryRepository.AccountBalanceRow row;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldMapProjectionRowsToResponse() {
        TenantContext.setTenantId(TENANT_ID);
        UUID accountId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        when(row.getAccountId()).thenReturn(accountId);
        when(row.getAccountCode()).thenReturn("3.1");
        when(row.getAccountName()).thenReturn("Receita Bruta de Vendas");
        when(row.getAccountType()).thenReturn("REVENUE");
        when(row.getCurrencyCode()).thenReturn("BRL");
        when(row.getAmount()).thenReturn(new BigDecimal("900.00"));
        when(entryRepository.findAccountBalances(TENANT_ID, from, to, "BRL")).thenReturn(List.of(row));

        List<AccountBalanceResponse> response =
                new ListAccountBalancesUseCase(entryRepository).execute(new AccountBalanceCriteria(from, to, "brl"));

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().accountId()).isEqualTo(accountId);
        assertThat(response.getFirst().accountCode()).isEqualTo("3.1");
        assertThat(response.getFirst().accountType()).isEqualTo(AccountingAccountType.REVENUE);
        assertThat(response.getFirst().amount()).isEqualByComparingTo("900.00");
    }
}
