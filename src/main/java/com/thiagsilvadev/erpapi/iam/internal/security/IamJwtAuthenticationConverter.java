package com.thiagsilvadev.erpapi.iam.internal.security;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AccessControlFacade;

public class IamJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final AccessControlFacade accessControlFacade;
    private final CredentialAuthenticationService credentials;

    public IamJwtAuthenticationConverter(AccessControlFacade facade, CredentialAuthenticationService credentials) {
        this.accessControlFacade = facade;
        this.credentials = credentials;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        if (!credentials.isActive(UUID.fromString(jwt.getSubject())))
            throw new InvalidBearerTokenException("Credential is not active");
        try {
            String tokenUse = jwt.getClaimAsString("token_use");
            if ("platform".equals(tokenUse)) {
                UUID userId = parseUuid(jwt.getSubject());
                UUID assignmentId = parseUuid(jwt.getClaimAsString("platform_assignment_id"));
                Long version = longClaim(jwt, "authz_version");
                if (userId == null || assignmentId == null || version == null) {
                    throw new InvalidBearerTokenException("Required authorization claims are missing");
                }
                var authorization = accessControlFacade.validatePlatform(assignmentId, userId, version);
                List<GrantedAuthority> authorities = Stream.concat(
                                authorization.permissions().stream(), Stream.of("ROLE_" + authorization.role()))
                        .map(SimpleGrantedAuthority::new)
                        .map(GrantedAuthority.class::cast)
                        .toList();
                return new JwtAuthenticationToken(jwt, authorities);
            }
            if ("global".equals(tokenUse)) return new JwtAuthenticationToken(jwt, List.of());
            if (!"scoped".equals(tokenUse)) throw new InvalidBearerTokenException("Unknown token context");
            UUID userId = parseUuid(jwt.getSubject());
            UUID companyId = parseUuid(jwt.getClaimAsString(TenantContextFilter.TENANT_ID_CLAIM));
            UUID membershipId = parseUuid(jwt.getClaimAsString("membership_id"));
            Long version = longClaim(jwt, "authz_version");
            if (userId == null || companyId == null || membershipId == null || version == null) {
                throw new InvalidBearerTokenException("Required authorization claims are missing");
            }
            var authorization = accessControlFacade.validateScopedToken(membershipId, userId, companyId, version);
            List<GrantedAuthority> authorities = Stream.concat(
                            authorization.permissions().stream(),
                            authorization.roles().stream().map(role -> "ROLE_" + role.code()))
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .toList();
            return new JwtAuthenticationToken(jwt, authorities);
        } catch (AccessDeniedException exception) {
            throw new InvalidBearerTokenException("Authorization is no longer valid", exception);
        }
    }

    private static UUID parseUuid(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static Long longClaim(Jwt jwt, String claim) {
        if (!(jwt.getClaim(claim) instanceof Number value)) {
            return null;
        }
        return value.longValue();
    }
}
