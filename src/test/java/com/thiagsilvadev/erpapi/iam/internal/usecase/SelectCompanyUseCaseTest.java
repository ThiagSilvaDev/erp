package com.thiagsilvadev.erpapi.iam.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyRequest;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;
import com.thiagsilvadev.erpapi.user.api.UserMembershipFacade;
import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;
import com.thiagsilvadev.erpapi.user.api.UserMembershipStatus;
import com.thiagsilvadev.erpapi.user.api.UserRole;

@ExtendWith(MockitoExtension.class)
class SelectCompanyUseCaseTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserMembershipFacade userMembershipFacade;

    @Test
    void shouldIssueScopedTokenForActiveMembership() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Jwt jwt = jwt(userId, "global");
        var membership = new UserMembershipSnapshot(userId, companyId, UserRole.MANAGER, UserMembershipStatus.ACTIVE);
        when(userMembershipFacade.findMembership(userId, companyId)).thenReturn(Optional.of(membership));
        when(jwtTokenService.issueScoped(any(), eq(membership))).thenReturn(new IssuedToken("scoped-token", 3600));

        var response = useCase().execute(new SelectCompanyRequest(companyId), jwt);

        assertThat(response.accessToken()).isEqualTo("scoped-token");
        assertThat(response.activeCompanyId()).isEqualTo(companyId);
        assertThat(response.role()).isEqualTo(UserRole.MANAGER);
    }

    @Test
    void shouldRejectScopedTokenForCompanySelection() {
        UUID companyId = UUID.randomUUID();

        assertThatThrownBy(
                        () -> useCase().execute(new SelectCompanyRequest(companyId), jwt(UUID.randomUUID(), "scoped")))
                .isInstanceOf(AccessDeniedException.class);

        verify(jwtTokenService, never()).issueScoped(any(), any());
    }

    @Test
    void shouldRejectInactiveMembership() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        var membership = new UserMembershipSnapshot(userId, companyId, UserRole.MANAGER, UserMembershipStatus.INACTIVE);
        when(userMembershipFacade.findMembership(userId, companyId)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> useCase().execute(new SelectCompanyRequest(companyId), jwt(userId, "global")))
                .isInstanceOf(AccessDeniedException.class);

        verify(jwtTokenService, never()).issueScoped(any(), any());
    }

    private SelectCompanyUseCase useCase() {
        return new SelectCompanyUseCase(jwtTokenService, userMembershipFacade);
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
