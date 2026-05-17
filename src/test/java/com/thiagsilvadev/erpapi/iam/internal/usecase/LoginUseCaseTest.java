package com.thiagsilvadev.erpapi.iam.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginResponse;
import com.thiagsilvadev.erpapi.iam.internal.exception.InvalidCredentialsException;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase(jwtTokenService, authenticationManager);
    }

    @Test
    void shouldLoginAndReturnJwt() {
        UUID userId = UUID.randomUUID();
        IamUserDetails principal = new IamUserDetails(userId, "user@example.com", "hash", "ADMIN", true);
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("User@Example.com", "secret123")))
                .thenReturn(authentication);
        when(jwtTokenService.issue(principal)).thenReturn(new IssuedToken("jwt-token", 3600));

        LoginResponse response = useCase.execute(new LoginRequest("User@Example.com", "secret123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600);
        assertThat(response.user().id()).isEqualTo(userId);
        assertThat(response.user().email()).isEqualTo("user@example.com");
        assertThat(response.user().role()).isEqualTo("ADMIN");
    }

    @Test
    void shouldRejectUnknownEmail() {
        when(authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("user@example.com", "secret123")))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> useCase.execute(new LoginRequest("user@example.com", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtTokenService, never()).issue(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectInvalidPassword() {
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("user@example.com", "wrong")))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> useCase.execute(new LoginRequest("user@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtTokenService, never()).issue(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectInactiveCredential() {
        when(authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("user@example.com", "secret123")))
                .thenThrow(new DisabledException("User is disabled"));

        assertThatThrownBy(() -> useCase.execute(new LoginRequest("user@example.com", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtTokenService, never()).issue(org.mockito.ArgumentMatchers.any());
    }
}
