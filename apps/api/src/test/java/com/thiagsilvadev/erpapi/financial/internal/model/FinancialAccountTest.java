package com.thiagsilvadev.erpapi.financial.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.InvalidAttributeException;

class FinancialAccountTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();

    @Test
    void shouldCreateValidAccount() {
        FinancialAccount account = FinancialAccount.create(
                COMPANY_ID, " Caixa Loja Principal ", FinancialAccountType.CASH_REGISTER, "   Main cash   ");

        assertThat(account.getName()).isEqualTo("Caixa Loja Principal");
        assertThat(account.getType()).isEqualTo(FinancialAccountType.CASH_REGISTER);
        assertThat(account.getDescription()).isEqualTo("Main cash");
        assertThat(account.getStatus()).isEqualTo(FinancialResourceStatus.ACTIVE);
    }

    @Test
    void shouldCreateAccountWithoutDescription() {
        FinancialAccount account =
                FinancialAccount.create(COMPANY_ID, "Itaú PJ", FinancialAccountType.BANK_ACCOUNT, null);

        assertThat(account.getName()).isEqualTo("Itaú PJ");
        assertThat(account.getType()).isEqualTo(FinancialAccountType.BANK_ACCOUNT);
        assertThat(account.getDescription()).isNull();
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        assertThatThrownBy(() -> FinancialAccount.create(COMPANY_ID, "   ", FinancialAccountType.BANK_ACCOUNT, null))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("Financial account name cannot be blank");
    }

    @Test
    void shouldFailWhenTypeIsNull() {
        assertThatThrownBy(() -> FinancialAccount.create(COMPANY_ID, "Itaú PJ", null, null))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("Financial account type cannot be null");
    }

    @Test
    void shouldActivateAndDeactivate() {
        FinancialAccount account =
                FinancialAccount.create(COMPANY_ID, "Itaú PJ", FinancialAccountType.BANK_ACCOUNT, null);
        assertThat(account.getStatus()).isEqualTo(FinancialResourceStatus.ACTIVE);

        account.deactivate();
        assertThat(account.getStatus()).isEqualTo(FinancialResourceStatus.INACTIVE);

        account.activate();
        assertThat(account.getStatus()).isEqualTo(FinancialResourceStatus.ACTIVE);
    }
}
