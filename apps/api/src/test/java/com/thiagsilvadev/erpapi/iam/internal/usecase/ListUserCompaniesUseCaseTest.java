package com.thiagsilvadev.erpapi.iam.internal.usecase;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;

import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AccessControlFacade;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;

class ListUserCompaniesUseCaseTest {

    private final AccessControlFacade authorizationService = mock(AccessControlFacade.class);
    private final CompanyFacade companyFacade = mock(CompanyFacade.class);
    private final ListUserCompaniesUseCase useCase = new ListUserCompaniesUseCase(authorizationService, companyFacade);

    @Test
    void shouldListRolesForActiveCompanies() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        var membership = new TenantAuthorizationSnapshot(
                UUID.randomUUID(),
                userId,
                companyId,
                ACTIVE,
                1,
                List.of(new TenantAuthorizationSnapshot.RoleSummary(UUID.randomUUID(), "MANAGER", "Manager")),
                Set.of());
        when(authorizationService.getActiveMemberships(userId)).thenReturn(List.of(membership));
        when(companyFacade.getCompany(companyId))
                .thenReturn(new CompanySnapshot(
                        companyId,
                        1L,
                        "Acme Ltda",
                        "Acme",
                        null,
                        CompanyStatus.ACTIVE,
                        CompanyType.FACTORY,
                        null,
                        null,
                        null));

        var response = useCase.execute(jwt(userId, "global"));

        assertThat(response.getFirst().roles()).extracting("code").containsExactly("MANAGER");
    }

    @Test
    void shouldRequireGlobalAuthentication() {
        assertThatThrownBy(() -> useCase.execute(null)).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
        assertThatThrownBy(() -> useCase.execute(jwt(UUID.randomUUID(), "scoped")))
                .isInstanceOf(AccessDeniedException.class);
    }

    private static Jwt jwt(UUID userId, String tokenUse) {
        Instant now = Instant.parse("2026-05-26T00:00:00Z");
        return new Jwt(
                "token",
                now,
                now.plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", userId.toString(), "token_use", tokenUse));
    }
}
