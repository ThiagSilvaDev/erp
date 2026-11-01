package com.thiagsilvadev.erpapi.iam.internal.usecase;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AccessControlFacade;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.internal.dto.AuthUserResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.LoginRequest;
import com.thiagsilvadev.erpapi.iam.internal.exception.InvalidCredentialsException;
import com.thiagsilvadev.erpapi.iam.internal.exception.NoActiveMembershipException;
import com.thiagsilvadev.erpapi.iam.internal.mapper.AuthMapper;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtTokenService jwtTokenService;

    @Mock
    AccessControlFacade authorizationService;

    @Mock
    CompanyFacade companyFacade;

    @Mock
    AuthMapper authMapper;

    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase(
                jwtTokenService, authenticationManager, authorizationService, companyFacade, authMapper);
    }

    @Test
    void shouldLoginAndAutoScopeSingleMembership() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        IamUserDetails principal = new IamUserDetails(userId, "user@example.com", "hash", true);
        authenticate(principal);
        var membership = snapshot(userId, companyId, "OWNER");
        when(authorizationService.getActiveMemberships(userId)).thenReturn(List.of(membership));
        when(companyFacade.getCompany(companyId)).thenReturn(company(companyId));
        when(jwtTokenService.issueGlobal(principal)).thenReturn(new IssuedToken("global", 3600));
        when(jwtTokenService.issueScoped(principal, membership)).thenReturn(new IssuedToken("scoped", 3600));
        when(authMapper.toAuthUserResponse(principal)).thenReturn(new AuthUserResponse(userId, "user@example.com"));

        var response = useCase.execute(new LoginRequest("user@example.com", "secret123"));

        assertThat(response.accessToken()).isEqualTo("global");
        assertThat(response.scopedToken().accessToken()).isEqualTo("scoped");
        assertThat(response.availableCompanies().getFirst().roles())
                .extracting("code")
                .containsExactly("OWNER");
    }

    @Test
    void shouldNotAutoScopeMultipleMemberships() {
        UUID userId = UUID.randomUUID();
        IamUserDetails principal = new IamUserDetails(userId, "user@example.com", "hash", true);
        authenticate(principal);
        var first = snapshot(userId, UUID.randomUUID(), "OWNER");
        var second = snapshot(userId, UUID.randomUUID(), "MANAGER");
        when(authorizationService.getActiveMemberships(userId)).thenReturn(List.of(first, second));
        when(companyFacade.getCompany(first.companyId())).thenReturn(company(first.companyId()));
        when(companyFacade.getCompany(second.companyId())).thenReturn(company(second.companyId()));
        when(jwtTokenService.issueGlobal(principal)).thenReturn(new IssuedToken("global", 3600));
        when(authMapper.toAuthUserResponse(principal)).thenReturn(new AuthUserResponse(userId, "user@example.com"));

        assertThat(useCase.execute(new LoginRequest("user@example.com", "secret123"))
                        .scopedToken())
                .isNull();
    }

    @Test
    void shouldRejectNoMemberships() {
        UUID userId = UUID.randomUUID();
        IamUserDetails principal = new IamUserDetails(userId, "user@example.com", "hash", true);
        authenticate(principal);
        when(authorizationService.getActiveMemberships(userId)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(new LoginRequest("user@example.com", "secret123")))
                .isInstanceOf(NoActiveMembershipException.class);
        verify(jwtTokenService, never()).issueGlobal(any());
    }

    @Test
    void shouldRejectBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));
        assertThatThrownBy(() -> useCase.execute(new LoginRequest("user@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private void authenticate(IamUserDetails principal) {
        when(authenticationManager.authenticate(any()))
                .thenReturn(
                        UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
    }

    private static TenantAuthorizationSnapshot snapshot(UUID userId, UUID companyId, String code) {
        return new TenantAuthorizationSnapshot(
                UUID.randomUUID(),
                userId,
                companyId,
                ACTIVE,
                1,
                List.of(new TenantAuthorizationSnapshot.RoleSummary(UUID.randomUUID(), code, code)),
                Set.of("catalog:read"));
    }

    private static CompanySnapshot company(UUID id) {
        return new CompanySnapshot(
                id, 1L, "Acme Ltda", "Acme", null, CompanyStatus.ACTIVE, CompanyType.FACTORY, null, null, null);
    }
}
