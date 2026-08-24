package com.thiagsilvadev.erpapi.user.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.user.api.TenantAccessProvisioningFacade;
import com.thiagsilvadev.erpapi.user.internal.dto.CreateUserRequest;
import com.thiagsilvadev.erpapi.user.internal.event.UserCredentialProvisioningAfterCommitHandler;
import com.thiagsilvadev.erpapi.user.internal.event.UserEventPublisher;
import com.thiagsilvadev.erpapi.user.internal.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID ROLE_ID = UUID.randomUUID();

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    UserEventPublisher eventPublisher;

    @Mock
    UserCredentialProvisioningAfterCommitHandler credentialHandler;

    @Mock
    TenantAccessProvisioningFacade accessProvisioningFacade;

    @InjectMocks
    CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(COMPANY_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldRejectDuplicatedEmail() {
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(true);
        assertThatThrownBy(() -> useCase.execute(request())).isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldCreateGlobalUserAndProvisionCurrentTenantRoles() {
        UUID userId = UUID.randomUUID();
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", userId);
            return user;
        });

        useCase.execute(request());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        assertThat(userCaptor.getValue().getContact().getEmail()).isEqualTo("user@example.com");
        verify(accessProvisioningFacade).provision(userId, COMPANY_ID, Set.of(ROLE_ID));
        verify(credentialHandler).provisionAfterCommit(userCaptor.getValue(), "secret123");
        verify(eventPublisher).userCreated(userCaptor.getValue());
    }

    @Test
    void shouldTranslateDuplicateEmailRace() {
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("uk_user_email"));
        assertThatThrownBy(() -> useCase.execute(request())).isInstanceOf(EmailAlreadyExistsException.class);
        verify(accessProvisioningFacade, never()).provision(any(), any(), any());
    }

    private static CreateUserRequest request() {
        return new CreateUserRequest(
                "User", "Example", " User@Example.com ", "11912345678", null, Set.of(ROLE_ID), "secret123");
    }
}
