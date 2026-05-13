package com.thiagsilvadev.erpapi.iam.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginResponse;
import com.thiagsilvadev.erpapi.iam.internal.exception.InvalidCredentialsException;
import com.thiagsilvadev.erpapi.iam.internal.model.IamCredential;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private IamCredentialRepository credentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private LoginUseCase useCase;

    @Test
    void shouldLoginAndReturnJwt() {
        UUID userId = UUID.randomUUID();
        IamCredential credential = IamCredential.create(userId, " user@example.com ", "hash", "ADMIN");
        when(credentialRepository.findByEmailValue("user@example.com")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("secret123", "hash")).thenReturn(true);
        when(jwtTokenService.issue(credential)).thenReturn(new IssuedToken("jwt-token", 3600));

        LoginResponse response = useCase.execute(new LoginRequest("User@Example.com", "secret123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    void shouldRejectUnknownEmail() {
        when(credentialRepository.findByEmailValue("user@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new LoginRequest("user@example.com", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtTokenService, never()).issue(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectInvalidPassword() {
        IamCredential credential = IamCredential.create(UUID.randomUUID(), "user@example.com", "hash", "ADMIN");
        when(credentialRepository.findByEmailValue("user@example.com")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(new LoginRequest("user@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtTokenService, never()).issue(credential);
    }

    @Test
    void shouldRejectInactiveCredential() {
        IamCredential credential = IamCredential.create(UUID.randomUUID(), "user@example.com", "hash", "ADMIN");
        credential.inactivate();
        when(credentialRepository.findByEmailValue("user@example.com")).thenReturn(Optional.of(credential));

        assertThatThrownBy(() -> useCase.execute(new LoginRequest("user@example.com", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtTokenService, never()).issue(credential);
    }
}
