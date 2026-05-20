package com.thiagsilvadev.erpapi.financial.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class FinancialAccountTest {

    @Test
    void shouldCreateValidAccount() {
        FinancialAccount account = FinancialAccount.create(
                " Caixa Loja Principal ", FinancialAccountType.CASH_REGISTER, "   Main cash   ");

        assertThat(account.getName()).isEqualTo("Caixa Loja Principal");
        assertThat(account.getType()).isEqualTo(FinancialAccountType.CASH_REGISTER);
        assertThat(account.getDescription()).isEqualTo("Main cash");
        assertThat(account.getStatus()).isEqualTo(FinancialResourceStatus.ACTIVE);
    }

    @Test
    void shouldCreateAccountWithoutDescription() {
        FinancialAccount account = FinancialAccount.create("Itaú PJ", FinancialAccountType.BANK_ACCOUNT, null);

        assertThat(account.getName()).isEqualTo("Itaú PJ");
        assertThat(account.getType()).isEqualTo(FinancialAccountType.BANK_ACCOUNT);
        assertThat(account.getDescription()).isNull();
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        assertThatThrownBy(() -> FinancialAccount.create("   ", FinancialAccountType.BANK_ACCOUNT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Financial account name is required");
    }

    @Test
    void shouldFailWhenTypeIsNull() {
        assertThatThrownBy(() -> FinancialAccount.create("Itaú PJ", null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Financial account type is required");
    }

    @Test
    void shouldActivateAndDeactivate() {
        FinancialAccount account = FinancialAccount.create("Itaú PJ", FinancialAccountType.BANK_ACCOUNT, null);
        assertThat(account.getStatus()).isEqualTo(FinancialResourceStatus.ACTIVE);

        account.deactivate();
        assertThat(account.getStatus()).isEqualTo(FinancialResourceStatus.INACTIVE);

        account.activate();
        assertThat(account.getStatus()).isEqualTo(FinancialResourceStatus.ACTIVE);
    }
}
