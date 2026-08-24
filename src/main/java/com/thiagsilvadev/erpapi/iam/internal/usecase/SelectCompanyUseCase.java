package com.thiagsilvadev.erpapi.iam.internal.usecase;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AccessControlFacade;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.internal.dto.AuthRoleResponse;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyRequest;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectCompanyResponse;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService.IssuedToken;

@Component
public class SelectCompanyUseCase {

    private final JwtTokenService jwtTokenService;
    private final AccessControlFacade accessControlFacade;

    public SelectCompanyUseCase(JwtTokenService jwtTokenService, AccessControlFacade accessControlFacade) {
        this.jwtTokenService = jwtTokenService;
        this.accessControlFacade = accessControlFacade;
    }

    @Transactional(readOnly = true)
    public SelectCompanyResponse execute(SelectCompanyRequest request, Jwt jwt) {
        if (jwt == null || !"global".equals(jwt.getClaimAsString("token_use"))) {
            throw new AccessDeniedException("A global token is required to select an active company.");
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        TenantAuthorizationSnapshot membership;
        try {
            membership = accessControlFacade.getActiveMembership(userId, request.companyId());
        } catch (ResourceNotFoundException exception) {
            throw new AccessDeniedException("User does not have access to the requested company.");
        }

        IamUserDetails principal = new IamUserDetails(userId, jwt.getClaimAsString("email"), "", true);
        IssuedToken token = jwtTokenService.issueScoped(principal, membership);
        return SelectCompanyResponse.bearer(
                token.value(),
                token.expiresIn(),
                membership.companyId(),
                membership.roles().stream()
                        .map(role -> new AuthRoleResponse(role.id(), role.code(), role.name()))
                        .toList(),
                membership.permissions());
    }
}
