package com.thiagsilvadev.erpapi.iam.internal.usecase;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyResponse;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;
import com.thiagsilvadev.erpapi.user.api.UserMembershipFacade;
import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;
import com.thiagsilvadev.erpapi.user.api.UserMembershipStatus;

@Component
public class SelectCompanyUseCase {

    private final JwtTokenService jwtTokenService;
    private final UserMembershipFacade userMembershipFacade;

    public SelectCompanyUseCase(JwtTokenService jwtTokenService, UserMembershipFacade userMembershipFacade) {
        this.jwtTokenService = jwtTokenService;
        this.userMembershipFacade = userMembershipFacade;
    }

    @Transactional(readOnly = true)
    public SelectCompanyResponse execute(SelectCompanyRequest request, Jwt jwt) {
        if (jwt == null || !"global".equals(jwt.getClaimAsString("token_use"))) {
            throw new AccessDeniedException("A global token is required to select an active company.");
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        UserMembershipSnapshot membership = userMembershipFacade
                .findMembership(userId, request.companyId())
                .filter(candidate -> candidate.status() == UserMembershipStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedException("User does not have access to the requested company."));

        IamUserDetails principal = new IamUserDetails(
                userId, jwt.getClaimAsString("email"), "", membership.role().name(), true);
        IssuedToken token = jwtTokenService.issueScoped(principal, membership);
        return SelectCompanyResponse.bearer(
                token.value(), token.expiresIn(), membership.companyId(), membership.role());
    }
}
