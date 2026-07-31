package com.thiagsilvadev.erpapi.iam.internal.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyStatus;
import com.thiagsilvadev.erpapi.company.api.CompanyType;
import com.thiagsilvadev.erpapi.user.api.UserMembershipFacade;
import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;
import com.thiagsilvadev.erpapi.user.api.UserMembershipStatus;
import com.thiagsilvadev.erpapi.user.api.UserRole;

@ExtendWith(MockitoExtension.class)
class ListUserCompaniesUseCaseTest {

    @Mock
    private UserMembershipFacade userMembershipFacade;

    @Mock
    private CompanyFacade companyFacade;

    @Test
    void shouldListActiveUserCompanies() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        var membership = new UserMembershipSnapshot(userId, companyId, UserRole.MANAGER, UserMembershipStatus.ACTIVE);
        when(userMembershipFacade.getActiveMemberships(userId)).thenReturn(List.of(membership));
        when(companyFacade.getCompany(companyId)).thenReturn(company(companyId));

        var response = useCase().execute(jwt(userId, "global"));

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().companyId()).isEqualTo(companyId);
        assertThat(response.getFirst().legalName()).isEqualTo("Acme Ltda");
        assertThat(response.getFirst().tradeName()).isEqualTo("Acme");
        assertThat(response.getFirst().role()).isEqualTo(UserRole.MANAGER.name());
    }

    @Test
    void shouldRejectMissingAuthentication() {
        var exception = catchThrowableOfType(AuthenticationCredentialsNotFoundException.class, () -> useCase()
                .execute(null));

        assertThat(exception).isNotNull();
        verify(userMembershipFacade, never()).getActiveMemberships(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectScopedToken() {
        Jwt jwt = jwt(UUID.randomUUID(), "scoped");

        var exception = catchThrowableOfType(
                AccessDeniedException.class, () -> useCase().execute(jwt));

        assertThat(exception).isNotNull();
        verify(userMembershipFacade, never()).getActiveMemberships(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldFailWhenMembershipCompanyDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        var membership = new UserMembershipSnapshot(userId, companyId, UserRole.MANAGER, UserMembershipStatus.ACTIVE);
        Jwt jwt = jwt(userId, "global");
        when(userMembershipFacade.getActiveMemberships(userId)).thenReturn(List.of(membership));
        when(companyFacade.getCompany(companyId)).thenThrow(new ResourceNotFoundException("Company", companyId));

        var exception = catchThrowableOfType(
                ResourceNotFoundException.class, () -> useCase().execute(jwt));

        assertThat(exception).isNotNull();
    }

    private ListUserCompaniesUseCase useCase() {
        return new ListUserCompaniesUseCase(userMembershipFacade, companyFacade);
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

    private static CompanySnapshot company(UUID companyId) {
        return new CompanySnapshot(
                companyId, 1L, "Acme Ltda", "Acme", null, CompanyStatus.ACTIVE, CompanyType.FACTORY, null, null, null);
    }
}
