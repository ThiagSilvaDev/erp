package com.thiagsilvadev.erpapi.user.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningFacade;
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningRequest;
import com.thiagsilvadev.erpapi.user.api.UserRole;
import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.event.UserEventPublisher;
import com.thiagsilvadev.erpapi.user.internal.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.model.UserMembership;
import com.thiagsilvadev.erpapi.user.internal.repository.UserMembershipRepository;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyFacade companyFacade;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserEventPublisher eventPublisher;

    @Mock
    private CredentialProvisioningFacade credentialProvisioningFacade;

    @Mock
    private UserMembershipRepository userMembershipRepository;

    @InjectMocks
    private CreateUserUseCase useCase;

    @Test
    void shouldRejectDuplicatedEmail() {
        CreateUserRequest request = userRequest();
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).saveAndFlush(any());
        verify(credentialProvisioningFacade, never()).provision(any());
        verify(eventPublisher, never()).userCreated(any());
    }

    @Test
    void shouldRejectMissingCompany() {
        CreateUserRequest request = userRequest();
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(false);
        doThrow(new ResourceNotFoundException("Company", request.companyId()))
                .when(companyFacade)
                .requireCompanyExistsById(request.companyId());

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(exception -> assertThat(((ResourceNotFoundException) exception).getProperties())
                        .containsEntry("resourceName", "Company"));

        verify(userRepository, never()).saveAndFlush(any());
        verify(credentialProvisioningFacade, never()).provision(any());
        verify(eventPublisher, never()).userCreated(any());
    }

    @Test
    void shouldCreateUserProvisionCredentialAndPublishSafeEvent() {
        CreateUserRequest request = userRequest();
        UUID userId = UUID.randomUUID();
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", userId);
            return user;
        });

        useCase.execute(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<CredentialProvisioningRequest> credentialCaptor =
                ArgumentCaptor.forClass(CredentialProvisioningRequest.class);
        verify(credentialProvisioningFacade).provision(credentialCaptor.capture());
        assertThat(credentialCaptor.getValue().userId()).isEqualTo(userId);
        assertThat(credentialCaptor.getValue().email()).isEqualTo("user@example.com");
        assertThat(credentialCaptor.getValue().rawPassword()).isEqualTo("secret123");
        assertThat(credentialCaptor.getValue().role()).isEqualTo("ADMIN");

        verify(eventPublisher).userCreated(userCaptor.capture());
        verify(userRepository).saveAndFlush(userCaptor.getValue());
        ArgumentCaptor<UserMembership> membershipCaptor = ArgumentCaptor.forClass(UserMembership.class);
        verify(userMembershipRepository).save(membershipCaptor.capture());
        assertThat(membershipCaptor.getValue().getUserId()).isEqualTo(userId);
        assertThat(membershipCaptor.getValue().getCompanyId()).isEqualTo(request.companyId());
        assertThat(membershipCaptor.getValue().getRole()).isEqualTo(UserRole.ADMIN);

        InOrder inOrder =
                inOrder(userRepository, userMembershipRepository, credentialProvisioningFacade, eventPublisher);
        inOrder.verify(userRepository).saveAndFlush(any(User.class));
        inOrder.verify(userMembershipRepository).save(any(UserMembership.class));
        inOrder.verify(credentialProvisioningFacade).provision(any());
        inOrder.verify(eventPublisher).userCreated(any(User.class));
    }

    @Test
    void shouldTranslateDuplicateEmailRace() {
        CreateUserRequest request = userRequest();
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("uk_user_email"));

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(EmailAlreadyExistsException.class);

        verify(credentialProvisioningFacade, never()).provision(any());
        verify(eventPublisher, never()).userCreated(any());
    }

    @Test
    void shouldNotPublishEventWhenCredentialProvisioningFails() {
        CreateUserRequest request = userRequest();
        UUID userId = UUID.randomUUID();
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", userId);
            return user;
        });
        doThrow(new IllegalStateException("credential failure"))
                .when(credentialProvisioningFacade)
                .provision(any());

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(IllegalStateException.class);

        verify(eventPublisher, never()).userCreated(any());
    }

    private static CreateUserRequest userRequest() {
        return new CreateUserRequest(
                "User",
                "Example",
                " User@Example.com ",
                "11912345678",
                null,
                UserRole.ADMIN,
                UUID.randomUUID(),
                "secret123");
    }
}
