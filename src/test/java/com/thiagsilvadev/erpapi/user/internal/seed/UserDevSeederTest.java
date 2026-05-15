package com.thiagsilvadev.erpapi.user.internal.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.model.Cnpj;
import com.thiagsilvadev.erpapi.common.model.Contact;
import com.thiagsilvadev.erpapi.common.model.address.Address;
import com.thiagsilvadev.erpapi.common.model.address.Country;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.user.api.UserRole;
import com.thiagsilvadev.erpapi.user.internal.event.UserEventPublisher;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDevSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyFacade companyFacade;

    @Mock
    private UserEventPublisher eventPublisher;

    @Test
    void shouldSeedAdminUserAndCredentialWhenMissing() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(userRepository.existsByContactEmailValue(UserDevSeeder.ADMIN_EMAIL))
                .thenReturn(false);
        when(companyFacade.findByCnpj("11.222.333/0001-81")).thenReturn(Optional.of(company(companyId)));
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", userId);
            return user;
        });

        new UserDevSeeder(userRepository, companyFacade, eventPublisher).run(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User user = userCaptor.getValue();
        assertThat(user.getFirstName()).isEqualTo("Admin");
        assertThat(user.getContact().getEmail()).isEqualTo(UserDevSeeder.ADMIN_EMAIL);
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(user.getCompanyId()).isEqualTo(companyId);

        verify(eventPublisher).userCreated(savedUser(userCaptor.getValue(), userId), UserDevSeeder.ADMIN_PASSWORD);
    }

    @Test
    void shouldSkipAdminUserWhenAlreadySeeded() {
        when(userRepository.existsByContactEmailValue(UserDevSeeder.ADMIN_EMAIL))
                .thenReturn(true);

        new UserDevSeeder(userRepository, companyFacade, eventPublisher).run(null);

        verify(companyFacade, never()).findByCnpj(org.mockito.ArgumentMatchers.anyString());
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher, never())
                .userCreated(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    private static User savedUser(User user, UUID id) {
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static CompanySnapshot company(UUID id) {
        return new CompanySnapshot(
                id,
                "Acme Industria Ltda",
                "Acme",
                Cnpj.create("11.222.333/0001-81"),
                CompanyStatus.ACTIVE,
                CompanyType.FACTORY,
                Contact.create("financeiro@acme.example", "11912345678"),
                Address.builder()
                        .postalCode("01310-100")
                        .street("Avenida Paulista")
                        .number("1000")
                        .district("Bela Vista")
                        .city("Sao Paulo")
                        .region("SP")
                        .country(Country.BR)
                        .build());
    }
}
