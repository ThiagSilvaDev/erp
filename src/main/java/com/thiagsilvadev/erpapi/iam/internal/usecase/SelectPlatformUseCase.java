package com.thiagsilvadev.erpapi.iam.internal.usecase;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AccessControlFacade;
import com.thiagsilvadev.erpapi.iam.internal.dto.SelectPlatformResponse;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.iam.internal.service.JwtTokenService;

@Component
public class SelectPlatformUseCase {

    private final AccessControlFacade accessControlFacade;
    private final JwtTokenService jwtTokenService;

    public SelectPlatformUseCase(AccessControlFacade accessControlFacade, JwtTokenService jwtTokenService) {
        this.accessControlFacade = accessControlFacade;
        this.jwtTokenService = jwtTokenService;
    }

    public SelectPlatformResponse execute(Jwt jwt) {
        if (jwt == null || !"global".equals(jwt.getClaimAsString("token_use"))) {
            throw new AccessDeniedException("A global token is required to select platform administration.");
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        var authorization = accessControlFacade.requireActivePlatform(userId);
        var principal = new IamUserDetails(userId, jwt.getClaimAsString("email"), "", true);
        var token = jwtTokenService.issuePlatform(principal, authorization);
        return new SelectPlatformResponse(
                token.value(), "Bearer", token.expiresIn(), authorization.role(), authorization.permissions());
    }
}
