package com.thiagsilvadev.erpapi.user.internal.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningFacade;
import com.thiagsilvadev.erpapi.user.api.TenantAccessProvisioningFacade;
import com.thiagsilvadev.erpapi.user.internal.event.UserEventPublisher;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

class UserDevSeederTest {

    @Test
    void shouldSeedGlobalUsersCredentialsAndTenantRoles() {
        UserRepository users = mock(UserRepository.class);
        CompanyFacade companies = mock(CompanyFacade.class);
        UserEventPublisher events = mock(UserEventPublisher.class);
        CredentialProvisioningFacade credentials = mock(CredentialProvisioningFacade.class);
        TenantAccessProvisioningFacade access = mock(TenantAccessProvisioningFacade.class);
        UUID acmeId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        when(companies.resolveCompany("97.986.234/0001-91")).thenReturn(company(acmeId));
        when(companies.resolveCompany("17.018.282/0001-57")).thenReturn(company(partnerId));
        when(users.existsByContactEmailValue(any())).thenReturn(false);
        when(users.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            return user;
        });

        new UserDevSeeder(users, companies, events, credentials, access).run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users, atLeastOnce()).saveAndFlush(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(user -> user.getContact().getEmail())
                .contains(UserDevSeeder.ADMIN_EMAIL);
        verify(access, atLeastOnce()).provisionBuiltIn(any(), any(), any());
        verify(credentials, atLeastOnce()).provision(any());
    }

    private static CompanySnapshot company(UUID id) {
        return new CompanySnapshot(
                id, 1L, "Acme Ltda", "Acme", null, CompanyStatus.ACTIVE, CompanyType.FACTORY, null, null, null);
    }
}
