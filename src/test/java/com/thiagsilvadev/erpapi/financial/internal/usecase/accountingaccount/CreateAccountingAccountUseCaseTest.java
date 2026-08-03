package com.thiagsilvadev.erpapi.financial.internal.usecase.accountingaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.accountingaccount.CreateAccountingAccountRequest;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccount;
import com.thiagsilvadev.erpapi.financial.internal.model.AccountingAccountType;
import com.thiagsilvadev.erpapi.financial.internal.repository.AccountingAccountRepository;

@ExtendWith(MockitoExtension.class)
class CreateAccountingAccountUseCaseTest {
    private static final UUID TENANT_ID = UUID.randomUUID();

    @Mock
    private AccountingAccountRepository accountRepository;

    @Mock
    private FinancialMapper mapper;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCreateAccount() {
        TenantContext.setTenantId(TENANT_ID);
        CreateAccountingAccountRequest request = new CreateAccountingAccountRequest(
                "7.1", "Outras Receitas", null, null, AccountingAccountType.REVENUE, null, true);
        when(accountRepository.existsByCompanyIdAndCodeIgnoreCase(TENANT_ID, "7.1"))
                .thenReturn(false);
        when(accountRepository.save(any(AccountingAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase().execute(request);

        ArgumentCaptor<AccountingAccount> captor = ArgumentCaptor.forClass(AccountingAccount.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getCompanyId()).isEqualTo(TENANT_ID);
        assertThat(captor.getValue().getCode()).isEqualTo("7.1");
        assertThat(captor.getValue().getType()).isEqualTo(AccountingAccountType.REVENUE);
    }

    @Test
    void shouldRejectDuplicateCode() {
        TenantContext.setTenantId(TENANT_ID);
        CreateAccountingAccountRequest request =
                new CreateAccountingAccountRequest("3.1", "Dup", null, null, AccountingAccountType.REVENUE, null, true);
        when(accountRepository.existsByCompanyIdAndCodeIgnoreCase(TENANT_ID, "3.1"))
                .thenReturn(true);

        assertThatThrownBy(() -> useCase().execute(request)).isInstanceOf(ConflictException.class);
    }

    private CreateAccountingAccountUseCase useCase() {
        return new CreateAccountingAccountUseCase(accountRepository, mapper);
    }
}
