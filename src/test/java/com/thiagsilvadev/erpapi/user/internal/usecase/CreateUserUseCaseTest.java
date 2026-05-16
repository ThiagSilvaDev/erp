package com.thiagsilvadev.erpapi.user.internal.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
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

    @InjectMocks
    private CreateUserUseCase useCase;

    @Test
    void shouldRejectDuplicatedEmail() {
        CreateUserRequest request = userRequest();
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher, never())
                .userCreated(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldRejectMissingCompany() {
        CreateUserRequest request = userRequest();
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(false);
        when(companyFacade.findById(request.companyId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher, never())
                .userCreated(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldCreateUserAndPublishEventWithRawPassword() {
        CreateUserRequest request = userRequest();
        User user = User.create(
                "User",
                "Example",
                Contact.create("user@example.com", "11912345678"),
                null,
                UserRole.ADMIN,
                request.companyId());
        User savedUser = user;
        when(userRepository.existsByContactEmailValue("user@example.com")).thenReturn(false);
        when(companyFacade.findById(request.companyId()))
                .thenReturn(Optional.of(
                        org.mockito.Mockito.mock(com.thiagsilvadev.erpapi.company.api.CompanySnapshot.class)));
        when(userMapper.toEntity(
                        org.mockito.ArgumentMatchers.eq(request), org.mockito.ArgumentMatchers.any(Contact.class)))
                .thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);

        useCase.execute(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(eventPublisher).userCreated(userCaptor.capture(), org.mockito.ArgumentMatchers.eq("secret123"));
        verify(userRepository).save(user);
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
