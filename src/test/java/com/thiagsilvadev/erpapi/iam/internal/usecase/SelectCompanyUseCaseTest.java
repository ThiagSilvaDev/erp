package com.thiagsilvadev.erpapi.iam.internal.usecase;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AccessControlFacade;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyRequest;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;

@ExtendWith(MockitoExtension.class)
class SelectCompanyUseCaseTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AccessControlFacade authorizationService;

    @Test
    void shouldIssueScopedTokenForActiveMembership() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        var membership = snapshot(userId, companyId);
        when(authorizationService.getActiveMembership(userId, companyId)).thenReturn(membership);
        when(jwtTokenService.issueScoped(any(), eq(membership))).thenReturn(new IssuedToken("scoped-token", 3600));

        var response = useCase().execute(new SelectCompanyRequest(companyId), jwt(userId, "global"));

        assertThat(response.accessToken()).isEqualTo("scoped-token");
        assertThat(response.activeCompanyId()).isEqualTo(companyId);
        assertThat(response.roles()).extracting("code").containsExactly("MANAGER");
    }

    @Test
    void shouldRejectScopedTokenForCompanySelection() {
        assertThatThrownBy(() -> useCase()
                        .execute(new SelectCompanyRequest(UUID.randomUUID()), jwt(UUID.randomUUID(), "scoped")))
                .isInstanceOf(AccessDeniedException.class);
        verify(jwtTokenService, never()).issueScoped(any(), any());
    }

    @Test
    void shouldHideMissingMembershipAsAccessDenied() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(authorizationService.getActiveMembership(userId, companyId))
                .thenThrow(new ResourceNotFoundException("TenantMembership", userId));

        assertThatThrownBy(() -> useCase().execute(new SelectCompanyRequest(companyId), jwt(userId, "global")))
                .isInstanceOf(AccessDeniedException.class);
    }

    private SelectCompanyUseCase useCase() {
        return new SelectCompanyUseCase(jwtTokenService, authorizationService);
    }

    private static TenantAuthorizationSnapshot snapshot(UUID userId, UUID companyId) {
        return new TenantAuthorizationSnapshot(
                UUID.randomUUID(),
                userId,
                companyId,
                ACTIVE,
                1,
                List.of(new TenantAuthorizationSnapshot.RoleSummary(UUID.randomUUID(), "MANAGER", "Manager")),
                Set.of("catalog:read"));
    }

    private static Jwt jwt(UUID userId, String tokenUse) {
        Instant now = Instant.parse("2026-05-26T00:00:00Z");
        return new Jwt(
                "token",
                now,
                now.plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", userId.toString(), "email", "user@example.com", "token_use", tokenUse));
    }
}
