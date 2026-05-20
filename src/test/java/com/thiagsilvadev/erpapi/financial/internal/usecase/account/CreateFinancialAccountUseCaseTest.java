package com.thiagsilvadev.erpapi.financial.internal.usecase.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.financial.internal.dto.FinancialMapper;
import com.thiagsilvadev.erpapi.financial.internal.dto.account.CreateFinancialAccountRequest;
import com.thiagsilvadev.erpapi.financial.internal.dto.account.FinancialAccountResponse;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialAccount;
import com.thiagsilvadev.erpapi.financial.internal.model.FinancialAccountType;
import com.thiagsilvadev.erpapi.financial.internal.repository.FinancialAccountRepository;

class CreateFinancialAccountUseCaseTest {

    @Test
    void shouldCreateAccountSuccessfully() {
        FinancialAccountRepository repository = mock(FinancialAccountRepository.class);
        FinancialMapper mapper = mock(FinancialMapper.class);
        CreateFinancialAccountUseCase useCase = new CreateFinancialAccountUseCase(repository, mapper);

        CreateFinancialAccountRequest request =
                new CreateFinancialAccountRequest("Itaú PJ", FinancialAccountType.BANK_ACCOUNT, "Main bank");

        when(repository.existsByNameIgnoreCase("Itaú PJ")).thenReturn(false);
        when(repository.save(any(FinancialAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinancialAccountResponse expectedResponse = mock(FinancialAccountResponse.class);
        when(mapper.toResponse(any(FinancialAccount.class))).thenReturn(expectedResponse);

        FinancialAccountResponse response = useCase.execute(request);

        assertThat(response).isEqualTo(expectedResponse);
        verify(repository).save(argThat(account -> {
            return account.getName().equals("Itaú PJ")
                    && account.getType() == FinancialAccountType.BANK_ACCOUNT
                    && account.getDescription().equals("Main bank");
        }));
    }

    @Test
    void shouldFailWhenAccountNameAlreadyExists() {
        FinancialAccountRepository repository = mock(FinancialAccountRepository.class);
        FinancialMapper mapper = mock(FinancialMapper.class);
        CreateFinancialAccountUseCase useCase = new CreateFinancialAccountUseCase(repository, mapper);

        CreateFinancialAccountRequest request =
                new CreateFinancialAccountRequest("  Itaú PJ  ", FinancialAccountType.BANK_ACCOUNT, null);

        when(repository.existsByNameIgnoreCase("Itaú PJ")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("A financial account with this name already exists.");

        verify(repository, never()).save(any());
    }
}
