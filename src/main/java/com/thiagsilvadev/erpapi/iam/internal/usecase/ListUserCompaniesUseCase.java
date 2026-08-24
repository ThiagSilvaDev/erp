package com.thiagsilvadev.erpapi.iam.internal.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AccessControlFacade;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.internal.dto.AuthRoleResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.UserCompanyResponse;

@Component
public class ListUserCompaniesUseCase {

    private final AccessControlFacade accessControlFacade;
    private final CompanyFacade companyFacade;

    public ListUserCompaniesUseCase(AccessControlFacade accessControlFacade, CompanyFacade companyFacade) {
        this.accessControlFacade = accessControlFacade;
        this.companyFacade = companyFacade;
    }

    @Transactional(readOnly = true)
    public List<UserCompanyResponse> execute(Jwt jwt) {
        if (jwt == null) {
            throw new AuthenticationCredentialsNotFoundException("Authentication is required to list user companies.");
        }
        if (!"global".equals(jwt.getClaimAsString("token_use"))) {
            throw new AccessDeniedException("A global token is required to list user companies.");
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        List<TenantAuthorizationSnapshot> memberships = accessControlFacade.getActiveMemberships(userId);

        return memberships.stream().map(this::toUserCompanyResponse).toList();
    }

    private UserCompanyResponse toUserCompanyResponse(TenantAuthorizationSnapshot membership) {
        var company = companyFacade.getCompany(membership.companyId());

        return new UserCompanyResponse(
                membership.companyId(),
                company.legalName(),
                company.tradeName(),
                company.cnpj() != null ? company.cnpj().getNumber() : null,
                membership.roles().stream()
                        .map(role -> new AuthRoleResponse(role.id(), role.code(), role.name()))
                        .toList());
    }
}
