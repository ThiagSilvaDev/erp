package com.thiagsilvadev.erpapi.iam.internal.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.thiagsilvadev.erpapi.iam.internal.config.IamJwtProperties;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.iam.internal.security.RolePermissionResolver;
import com.thiagsilvadev.erpapi.user.api.UserMembershipSnapshot;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final IamJwtProperties properties;
    private final Clock clock;
    private final RolePermissionResolver rolePermissionResolver;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            IamJwtProperties properties,
            Clock clock,
            RolePermissionResolver rolePermissionResolver) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.clock = clock;
        this.rolePermissionResolver = rolePermissionResolver;
    }

    public IssuedToken issueGlobal(IamUserDetails principal) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.getExpiration());

        UUID userId = Objects.requireNonNull(principal.getUserId(), "userId cannot be null");

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiresAt)
                .claim("token_use", "global")
                .claim("email", principal.getEmail())
                .build();

        return issue(claims);
    }

    public IssuedToken issueScoped(IamUserDetails principal, UserMembershipSnapshot membership) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.getExpiration());

        UUID userId = Objects.requireNonNull(principal.getUserId(), "userId cannot be null");

        List<String> authorities = rolePermissionResolver
                .resolve(membership.role().name())
                .stream()
                .sorted()
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiresAt)
                .claim("token_use", "scoped")
                .claim("email", principal.getEmail())
                .claim("company_id", membership.companyId().toString())
                .claim("role", membership.role().name())
                .claim("authorities", authorities)
                .build();

        return issue(claims);
    }

    private IssuedToken issue(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token =
                jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new IssuedToken(token, properties.getExpiration().toSeconds());
    }

    public record IssuedToken(String value, long expiresIn) {}
}
