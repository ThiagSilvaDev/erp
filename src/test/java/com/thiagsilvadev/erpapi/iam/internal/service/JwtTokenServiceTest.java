package com.thiagsilvadev.erpapi.iam.internal.service;

import static com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.PlatformAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantAuthorizationSnapshot;
import com.thiagsilvadev.erpapi.iam.internal.config.IamJwtProperties;
import com.thiagsilvadev.erpapi.iam.internal.security.IamUserDetails;
import com.thiagsilvadev.erpapi.user.api.UserPermissions;

class JwtTokenServiceTest {

    @Test
    void shouldIssueGlobalTokenWithoutTenantAuthorization() {
        var fixture = fixture();
        UUID userId = UUID.randomUUID();

        var token = fixture.service.issueGlobal(new IamUserDetails(userId, "user@example.com", "hash", true));

        assertThat(token.value()).isEqualTo("encoded-token");
        assertThat(fixture.encoder.claims)
                .containsEntry("sub", userId.toString())
                .containsEntry("token_use", "global")
                .doesNotContainKeys("membership_id", "tenant_id", "authorities");
    }

    @Test
    void shouldIssueScopedTokenWithMembershipVersionRolesAndAuthorities() {
        var fixture = fixture();
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        var authorization = new TenantAuthorizationSnapshot(
                membershipId,
                userId,
                companyId,
                ACTIVE,
                7,
                List.of(new TenantAuthorizationSnapshot.RoleSummary(roleId, "MANAGER", "Manager")),
                Set.of(UserPermissions.READ));

        fixture.service.issueScoped(new IamUserDetails(userId, "user@example.com", "hash", true), authorization);

        assertThat(fixture.encoder.claims)
                .containsEntry("token_use", "scoped")
                .containsEntry("tenant_id", companyId.toString())
                .containsEntry("membership_id", membershipId.toString())
                .containsEntry("authz_version", 7L);
        assertThat((Object) fixture.encoder.claims.get("roles")).asList().containsExactly("MANAGER");
        assertThat((Object) fixture.encoder.claims.get("authorities")).asList().containsExactly(UserPermissions.READ);
    }

    @Test
    void shouldIssuePlatformToken() {
        var fixture = fixture();
        UUID userId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        fixture.service.issuePlatform(
                new IamUserDetails(userId, "user@example.com", "hash", true),
                new PlatformAuthorizationSnapshot(assignmentId, userId, 3L, "PLATFORM_ADMIN", Set.of("platform:read")));
        assertThat(fixture.encoder.claims)
                .containsEntry("token_use", "platform")
                .containsEntry("platform_assignment_id", assignmentId.toString())
                .containsEntry("authz_version", 3L)
                .doesNotContainKeys("tenant_id", "membership_id");
    }

    private static Fixture fixture() {
        CapturingJwtEncoder encoder = new CapturingJwtEncoder();
        IamJwtProperties properties = new IamJwtProperties();
        properties.setIssuer("erp-api-test");
        properties.setExpiration(Duration.ofMinutes(15));
        return new Fixture(
                encoder,
                new JwtTokenService(
                        encoder, properties, Clock.fixed(Instant.parse("2026-05-11T00:00:00Z"), ZoneOffset.UTC)));
    }

    private record Fixture(CapturingJwtEncoder encoder, JwtTokenService service) {}

    private static class CapturingJwtEncoder implements JwtEncoder {

        private Map<String, Object> claims;

        @Override
        public Jwt encode(JwtEncoderParameters parameters) {
            claims = parameters.getClaims().getClaims();
            return new Jwt("encoded-token", null, null, Map.of("alg", "HS256"), claims);
        }
    }
}
