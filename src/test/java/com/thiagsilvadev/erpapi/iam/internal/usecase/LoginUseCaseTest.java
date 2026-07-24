package com.thiagsilvadev.erpapi.iam.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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

import com.thiagsilvadev.erpapi.iam.internal.dto.AuthCompanyResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.AuthMapper;
import com.thiagsilvadev.erpapi.iam.internal.dto.AuthUserResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginResponse;
import com.thiagsilvadev.erpapi.iam.internal.exception.InvalidCredentialsException;
import com.thiagsilvadev.erpapi.iam.internal.exception.NoActiveMembershipException;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;
import com.thiagsilvadev.erpapi.user.api.UserMembershipFacade;
import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;
import com.thiagsilvadev.erpapi.user.api.UserMembershipStatus;
import com.thiagsilvadev.erpapi.user.api.UserRole;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserMembershipFacade userMembershipFacade;

    @Mock
    private AuthMapper authMapper;

    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase(jwtTokenService, authenticationManager, userMembershipFacade, authMapper);
    }

    @Test
    void shouldLoginAndReturnJwt() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        IamUserDetails principal = new IamUserDetails(userId, "user@example.com", "hash", "ADMIN", true);
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("User@Example.com", "secret123")))
                .thenReturn(authentication);
        var membership = new UserMembershipSnapshot(userId, companyId, UserRole.ADMIN, UserMembershipStatus.ACTIVE);
        when(userMembershipFacade.findActiveMemberships(userId)).thenReturn(List.of(membership));
        when(jwtTokenService.issueGlobal(principal)).thenReturn(new IssuedToken("global-token", 3600));
        when(jwtTokenService.issueScoped(principal, membership)).thenReturn(new IssuedToken("scoped-token", 3600));
        when(authMapper.toAuthUserResponse(principal))
                .thenReturn(new AuthUserResponse(userId, "user@example.com", "ADMIN"));
        when(authMapper.toAuthCompanyResponse(membership))
                .thenReturn(new AuthCompanyResponse(companyId, UserRole.ADMIN));

        LoginResponse response = useCase.execute(new LoginRequest("User@Example.com", "secret123"));

        assertThat(response.accessToken()).isEqualTo("global-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600);
        assertThat(response.user().id()).isEqualTo(userId);
        assertThat(response.user().email()).isEqualTo("user@example.com");
        assertThat(response.user().role()).isEqualTo("ADMIN");
        assertThat(response.availableCompanies()).hasSize(1);
        assertThat(response.availableCompanies().getFirst().companyId()).isEqualTo(companyId);
        assertThat(response.scopedToken().accessToken()).isEqualTo("scoped-token");
    }

    @Test
    void shouldNotAutoIssueScopedTokenWhenUserHasMultipleCompanies() {
        UUID userId = UUID.randomUUID();
        IamUserDetails principal = new IamUserDetails(userId, "user@example.com", "hash", "ADMIN", true);
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("user@example.com", "secret123")))
                .thenReturn(authentication);
        when(userMembershipFacade.findActiveMemberships(userId))
                .thenReturn(List.of(
                        new UserMembershipSnapshot(
                                userId, UUID.randomUUID(), UserRole.ADMIN, UserMembershipStatus.ACTIVE),
                        new UserMembershipSnapshot(
                                userId, UUID.randomUUID(), UserRole.MANAGER, UserMembershipStatus.ACTIVE)));
        when(jwtTokenService.issueGlobal(principal)).thenReturn(new IssuedToken("global-token", 3600));
        when(authMapper.toAuthUserResponse(principal))
                .thenReturn(new AuthUserResponse(userId, "user@example.com", "ADMIN"));
        when(authMapper.toAuthCompanyResponse(any(UserMembershipSnapshot.class)))
                .thenAnswer(inv -> {
                    UserMembershipSnapshot m = inv.getArgument(0);
                    return new AuthCompanyResponse(m.companyId(), m.role());
                });

        LoginResponse response = useCase.execute(new LoginRequest("user@example.com", "secret123"));

        assertThat(response.accessToken()).isEqualTo("global-token");
        assertThat(response.availableCompanies()).hasSize(2);
        assertThat(response.scopedToken()).isNull();
    }

    @Test
    void shouldRejectLoginWhenUserHasNoActiveMemberships() {
        UUID userId = UUID.randomUUID();
        IamUserDetails principal = new IamUserDetails(userId, "user@example.com", "hash", "ADMIN", true);
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("user@example.com", "secret123")))
                .thenReturn(authentication);
        when(userMembershipFacade.findActiveMemberships(userId)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(new LoginRequest("user@example.com", "secret123")))
                .isInstanceOf(NoActiveMembershipException.class);

        verify(jwtTokenService, never()).issueGlobal(any());
        verify(authMapper, never()).toAuthUserResponse(any());
    }

    @Test
    void shouldRejectUnknownEmail() {
        when(authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("user@example.com", "secret123")))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> useCase.execute(new LoginRequest("user@example.com", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtTokenService, never()).issueGlobal(any());
        verify(authMapper, never()).toAuthUserResponse(any());
    }

    @Test
    void shouldRejectInvalidPassword() {
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("user@example.com", "wrong")))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> useCase.execute(new LoginRequest("user@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtTokenService, never()).issueGlobal(any());
        verify(authMapper, never()).toAuthUserResponse(any());
    }

    @Test
    void shouldRejectInactiveCredential() {
        when(authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("user@example.com", "secret123")))
                .thenThrow(new DisabledException("User is disabled"));

        assertThatThrownBy(() -> useCase.execute(new LoginRequest("user@example.com", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtTokenService, never()).issueGlobal(any());
        verify(authMapper, never()).toAuthUserResponse(any());
    }
}
