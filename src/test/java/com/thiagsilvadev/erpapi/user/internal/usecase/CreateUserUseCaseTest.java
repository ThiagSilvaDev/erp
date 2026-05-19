package com.thiagsilvadev.erpapi.user.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
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
import com.thiagsilvadev.erpapi.iam.api.CredentialProvisioningFacade;
import com.thiagsilvadev.erpapi.iam.api.CredentialProvisioningRequest;
import com.thiagsilvadev.erpapi.user.api.UserRole;
import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.event.UserEventPublisher;
import com.thiagsilvadev.erpapi.user.internal.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.model.User;
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

    @InjectMocks
    private CreateUserUseCase useCase;

    @Test
    void shouldRejectDuplicatedEmail() {
        CreateUserRequest request = userRequest();
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
        verify(credentialProvisioningFacade, never()).provision(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher, never()).userCreated(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectMissingCompany() {
        CreateUserRequest request = userRequest();
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(false);
        when(companyFacade.findById(request.companyId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
        verify(credentialProvisioningFacade, never()).provision(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher, never()).userCreated(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldCreateUserProvisionCredentialAndPublishSafeEvent() {
        CreateUserRequest request = userRequest();
        UUID userId = UUID.randomUUID();
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(false);
        when(companyFacade.findById(request.companyId()))
                .thenReturn(Optional.of(
                        org.mockito.Mockito.mock(com.thiagsilvadev.erpapi.company.api.CompanySnapshot.class)));
        when(userRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> {
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

        InOrder inOrder = inOrder(userRepository, credentialProvisioningFacade, eventPublisher);
        inOrder.verify(userRepository).saveAndFlush(org.mockito.ArgumentMatchers.any(User.class));
        inOrder.verify(credentialProvisioningFacade).provision(org.mockito.ArgumentMatchers.any());
        inOrder.verify(eventPublisher).userCreated(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void shouldTranslateDuplicateEmailRace() {
        CreateUserRequest request = userRequest();
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(false);
        when(companyFacade.findById(request.companyId()))
                .thenReturn(Optional.of(
                        org.mockito.Mockito.mock(com.thiagsilvadev.erpapi.company.api.CompanySnapshot.class)));
        when(userRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(User.class)))
                .thenThrow(new DataIntegrityViolationException("uk_user_email"));

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(EmailAlreadyExistsException.class);

        verify(credentialProvisioningFacade, never()).provision(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher, never()).userCreated(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldNotPublishEventWhenCredentialProvisioningFails() {
        CreateUserRequest request = userRequest();
        UUID userId = UUID.randomUUID();
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(false);
        when(companyFacade.findById(request.companyId()))
                .thenReturn(Optional.of(
                        org.mockito.Mockito.mock(com.thiagsilvadev.erpapi.company.api.CompanySnapshot.class)));
        when(userRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    ReflectionTestUtils.setField(user, "id", userId);
                    return user;
                });
        org.mockito.Mockito.doThrow(new IllegalStateException("credential failure"))
                .when(credentialProvisioningFacade)
                .provision(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(IllegalStateException.class);

        verify(eventPublisher, never()).userCreated(org.mockito.ArgumentMatchers.any());
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
