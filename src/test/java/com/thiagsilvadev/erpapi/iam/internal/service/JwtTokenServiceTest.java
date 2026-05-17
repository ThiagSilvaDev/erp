package com.thiagsilvadev.erpapi.iam.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.thiagsilvadev.erpapi.iam.internal.config.IamJwtProperties;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.user.api.UserPermissions;

class JwtTokenServiceTest {

    @Test
    void shouldIssueTokenWithIamPrincipalClaims() {
        Instant now = Instant.parse("2026-05-11T00:00:00Z");
        CapturingJwtEncoder jwtEncoder = new CapturingJwtEncoder();
        IamJwtProperties properties = new IamJwtProperties();
        properties.setIssuer("erp-api-test");
        properties.setExpiration(Duration.ofMinutes(15));
        JwtTokenService tokenService = new JwtTokenService(jwtEncoder, properties, Clock.fixed(now, ZoneOffset.UTC));
        UUID userId = UUID.randomUUID();
        IamUserDetails principal = new IamUserDetails(
                userId,
                "user@example.com",
                "hash",
                "ADMIN",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority(UserPermissions.WRITE)));

        JwtTokenService.IssuedToken issuedToken = tokenService.issue(principal);

        assertThat(issuedToken.value()).isEqualTo("encoded-token");
        assertThat(issuedToken.expiresIn()).isEqualTo(900);
        assertThat(jwtEncoder.claims).containsEntry("iss", "erp-api-test");
        assertThat(jwtEncoder.claims).containsEntry("sub", userId.toString());
        assertThat(jwtEncoder.claims).containsEntry("email", "user@example.com");
        assertThat(jwtEncoder.claims).containsEntry("role", "ADMIN");
        assertThat(jwtEncoder.claims.get("authorities")).isEqualTo(List.of("ROLE_ADMIN", UserPermissions.WRITE));
        assertThat(jwtEncoder.claims).containsEntry("iat", now);
        assertThat(jwtEncoder.claims).containsEntry("exp", now.plus(Duration.ofMinutes(15)));
        assertThat(jwtEncoder.claims).containsKey("jti");
    }

    private static class CapturingJwtEncoder implements JwtEncoder {

        private Map<String, Object> claims;

        @Override
        public Jwt encode(JwtEncoderParameters parameters) {
            claims = parameters.getClaims().getClaims();
            return new Jwt("encoded-token", null, null, Map.of("alg", "HS256"), claims);
        }
    }
}
