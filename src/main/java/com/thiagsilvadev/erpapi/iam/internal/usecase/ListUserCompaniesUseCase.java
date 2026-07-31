package com.thiagsilvadev.erpapi.iam.internal.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.iam.internal.dto.UserCompanyResponse;
import com.thiagsilvadev.erpapi.user.api.UserMembershipFacade;
import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;

@Component
public class ListUserCompaniesUseCase {

    private final UserMembershipFacade userMembershipFacade;
    private final CompanyFacade companyFacade;

    public ListUserCompaniesUseCase(UserMembershipFacade userMembershipFacade, CompanyFacade companyFacade) {
        this.userMembershipFacade = userMembershipFacade;
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
        List<UserMembershipSnapshot> memberships = userMembershipFacade.getActiveMemberships(userId);

        return memberships.stream().map(this::toUserCompanyResponse).toList();
    }

    private UserCompanyResponse toUserCompanyResponse(UserMembershipSnapshot membership) {
        var company = companyFacade.getCompany(membership.companyId());

        return new UserCompanyResponse(
                membership.companyId(),
                company.legalName(),
                company.tradeName(),
                company.cnpj() != null ? company.cnpj().getNumber() : null,
                membership.role().name());
    }
}
