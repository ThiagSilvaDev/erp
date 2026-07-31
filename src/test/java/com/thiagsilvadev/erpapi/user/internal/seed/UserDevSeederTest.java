package com.thiagsilvadev.erpapi.user.internal.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.common.domain.model.address.Address;
import com.thiagsilvadev.erpapi.common.domain.model.address.Country;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningFacade;
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningRequest;
import com.thiagsilvadev.erpapi.user.api.UserRole;
import com.thiagsilvadev.erpapi.user.internal.event.UserEventPublisher;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.model.UserMembership;
import com.thiagsilvadev.erpapi.user.internal.repository.UserMembershipRepository;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDevSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyFacade companyFacade;

    @Mock
    private UserEventPublisher eventPublisher;

    @Mock
    private CredentialProvisioningFacade credentialProvisioningFacade;

    @Mock
    private UserMembershipRepository userMembershipRepository;

    @Test
    void shouldSeedAdminUserAndCredentialWhenMissing() {
        UUID acmeId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(companyFacade.resolveCompany("97.986.234/0001-91")).thenReturn(company(acmeId));
        when(companyFacade.resolveCompany("17.018.282/0001-57")).thenReturn(company(partnerId));
        when(userMembershipRepository.findByUserIdAndCompanyId(any(), any()))
                .thenReturn(Optional.of(UserMembership.create(UUID.randomUUID(), UUID.randomUUID(), UserRole.ADMIN)));

        when(userRepository.existsByContactEmailValue(any())).thenReturn(true);
        when(userRepository.existsByContactEmailValue(UserDevSeeder.ADMIN_EMAIL))
                .thenReturn(false);
        when(userRepository.findByContactEmailValue(any()))
                .thenReturn(Optional.of(User.create(
                        "Skip",
                        "User",
                        Contact.create("skip@example.com", "11987654321"),
                        LocalDate.of(1990, 1, 1),
                        UserRole.OPERATOR,
                        acmeId)));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", userId);
            return user;
        });
        when(userMembershipRepository.findByUserIdAndCompanyId(userId, acmeId)).thenReturn(Optional.empty());
        when(userMembershipRepository.findByUserIdAndCompanyId(userId, partnerId))
                .thenReturn(Optional.empty());

        new UserDevSeeder(
                        userRepository,
                        companyFacade,
                        eventPublisher,
                        credentialProvisioningFacade,
                        userMembershipRepository)
                .run(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());

        User user = userCaptor.getValue();
        assertThat(user.getFirstName()).isEqualTo("Admin");
        assertThat(user.getContact().getEmail()).isEqualTo(UserDevSeeder.ADMIN_EMAIL);
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(user.getCompanyId()).isEqualTo(acmeId);
        ArgumentCaptor<UserMembership> membershipCaptor = ArgumentCaptor.forClass(UserMembership.class);
        verify(userMembershipRepository, times(2)).save(membershipCaptor.capture());
        assertThat(membershipCaptor.getAllValues())
                .extracting(UserMembership::getUserId)
                .containsExactly(userId, userId);
        assertThat(membershipCaptor.getAllValues())
                .extracting(UserMembership::getCompanyId)
                .containsExactly(acmeId, partnerId);
        assertThat(membershipCaptor.getAllValues())
                .extracting(UserMembership::getRole)
                .containsExactly(UserRole.ADMIN, UserRole.ADMIN);

        ArgumentCaptor<CredentialProvisioningRequest> credentialCaptor =
                ArgumentCaptor.forClass(CredentialProvisioningRequest.class);
        verify(credentialProvisioningFacade).provision(credentialCaptor.capture());
        assertThat(credentialCaptor.getValue().userId()).isEqualTo(userId);
        assertThat(credentialCaptor.getValue().email()).isEqualTo(UserDevSeeder.ADMIN_EMAIL);
        assertThat(credentialCaptor.getValue().rawPassword()).isEqualTo(UserDevSeeder.ADMIN_PASSWORD);
        assertThat(credentialCaptor.getValue().role()).isEqualTo("ADMIN");

        verify(eventPublisher).userCreated(savedUser(userCaptor.getValue(), userId));
    }

    @Test
    void shouldSeedMissingMembershipsWhenAdminUserAlreadyExists() {
        UUID acmeId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(companyFacade.resolveCompany("97.986.234/0001-91")).thenReturn(company(acmeId));
        when(companyFacade.resolveCompany("17.018.282/0001-57")).thenReturn(company(partnerId));

        when(userRepository.existsByContactEmailValue(any())).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMembershipRepository.findByUserIdAndCompanyId(any(), any()))
                .thenReturn(Optional.of(UserMembership.create(UUID.randomUUID(), acmeId, UserRole.ADMIN)));

        User adminUser = User.create(
                "Admin",
                "Acme",
                Contact.create(UserDevSeeder.ADMIN_EMAIL, "11987654321"),
                LocalDate.of(1990, 1, 1),
                UserRole.ADMIN,
                acmeId);
        ReflectionTestUtils.setField(adminUser, "id", userId);
        when(userRepository.existsByContactEmailValue(UserDevSeeder.ADMIN_EMAIL))
                .thenReturn(true);
        when(userRepository.findByContactEmailValue(UserDevSeeder.ADMIN_EMAIL)).thenReturn(Optional.of(adminUser));
        when(userMembershipRepository.findByUserIdAndCompanyId(userId, acmeId))
                .thenReturn(Optional.of(UserMembership.create(userId, acmeId, UserRole.ADMIN)));
        when(userMembershipRepository.findByUserIdAndCompanyId(userId, partnerId))
                .thenReturn(Optional.empty());

        new UserDevSeeder(
                        userRepository,
                        companyFacade,
                        eventPublisher,
                        credentialProvisioningFacade,
                        userMembershipRepository)
                .run(null);

        verify(userRepository, times(5)).saveAndFlush(any());
        verify(credentialProvisioningFacade, times(5)).provision(any());
        verify(eventPublisher, times(5)).userCreated(any());

        ArgumentCaptor<UserMembership> membershipCaptor = ArgumentCaptor.forClass(UserMembership.class);
        verify(userMembershipRepository).save(membershipCaptor.capture());
        assertThat(membershipCaptor.getValue().getUserId()).isEqualTo(userId);
        assertThat(membershipCaptor.getValue().getCompanyId()).isEqualTo(partnerId);
        assertThat(membershipCaptor.getValue().getRole()).isEqualTo(UserRole.ADMIN);
    }

    private static User savedUser(User user, UUID id) {
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static CompanySnapshot company(UUID id) {
        return new CompanySnapshot(
                id,
                1L,
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
                        .build(),
                null);
    }
}
