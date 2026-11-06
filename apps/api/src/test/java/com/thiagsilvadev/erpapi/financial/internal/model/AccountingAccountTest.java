package com.thiagsilvadev.erpapi.financial.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thiagsilvadev.erpapi.common.domain.exception.InvalidAttributeException;

class AccountingAccountTest {
    private static final UUID COMPANY_ID = UUID.randomUUID();

    @Test
    void shouldCreateValidAccount() {
        AccountingAccount account = AccountingAccount.create(
                COMPANY_ID,
                " 3.1 ",
                " Receita Bruta ",
                "  Sales  ",
                null,
                AccountingAccountType.REVENUE,
                AccountingSystemRole.REVENUE_SALES,
                true);

        assertThat(account.getCode()).isEqualTo("3.1");
        assertThat(account.getName()).isEqualTo("Receita Bruta");
        assertThat(account.getDescription()).isEqualTo("Sales");
        assertThat(account.getType()).isEqualTo(AccountingAccountType.REVENUE);
        assertThat(account.getSystemRole()).isEqualTo(AccountingSystemRole.REVENUE_SALES);
        assertThat(account.isAcceptsPostings()).isTrue();
        assertThat(account.getStatus()).isEqualTo(FinancialResourceStatus.ACTIVE);
        assertThat(account.canAcceptPostings()).isTrue();
    }

    @Test
    void shouldFailWhenCodeIsBlank() {
        assertThatThrownBy(() -> AccountingAccount.create(
                        COMPANY_ID, "  ", "Name", null, null, AccountingAccountType.EXPENSE, null, true))
                .isInstanceOf(InvalidAttributeException.class)
                .hasMessage("Accounting account code cannot be blank");
    }

    @Test
    void shouldNotAcceptPostingsWhenInactive() {
        AccountingAccount account = AccountingAccount.create(
                COMPANY_ID, "5.1", "Admin", null, null, AccountingAccountType.EXPENSE, null, true);
        account.deactivate();
        assertThat(account.canAcceptPostings()).isFalse();
    }
}
