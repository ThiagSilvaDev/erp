package com.thiagsilvadev.erpapi.iam.internal.config;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.StringUtils;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AccessControlFacade;
import com.thiagsilvadev.erpapi.iam.internal.security.CustomAccessDeniedHandler;
import com.thiagsilvadev.erpapi.iam.internal.security.CustomAuthenticationEntryPoint;
import com.thiagsilvadev.erpapi.iam.internal.security.TenantContextFilter;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(IamJwtProperties.class)
public class IamSecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler,
            AccessControlFacade accessControlFacade,
            TenantContextFilter tenantContextFilter) {
        return http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/auth/login")
                        .permitAll()
                        .requestMatchers("/api/v1/auth/select-company")
                        .permitAll()
                        .requestMatchers("/api/v1/auth/companies")
                        .permitAll()
                        .requestMatchers("/api/v1/auth/select-platform")
                        .permitAll()
                        .requestMatchers("/actuator/health/**")
                        .permitAll()
                        .requestMatchers("/api/v1/platform/**")
                        .access(tokenUse("platform"))
                        .requestMatchers("/api/v1/**")
                        .access(tokenUse("scoped"))
                        .requestMatchers("/v3/api-docs")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter(accessControlFacade))))
                .addFilterAfter(tenantContextFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter(
            AccessControlFacade accessControlFacade) {
        return jwt -> {
            String tokenUse = jwt.getClaimAsString("token_use");
            if ("platform".equals(tokenUse)) {
                UUID userId = parseUuid(jwt.getSubject());
                UUID assignmentId = parseUuid(jwt.getClaimAsString("platform_assignment_id"));
                Long version = longClaim(jwt, "authz_version");
                if (userId == null || assignmentId == null || version == null) {
                    return new JwtAuthenticationToken(jwt, List.of());
                }
                var authorization = accessControlFacade.validatePlatform(assignmentId, userId, version);
                List<GrantedAuthority> authorities = Stream.concat(
                                authorization.permissions().stream(), Stream.of("ROLE_" + authorization.role()))
                        .map(SimpleGrantedAuthority::new)
                        .map(GrantedAuthority.class::cast)
                        .toList();
                return new JwtAuthenticationToken(jwt, authorities);
            }
            if (!"scoped".equals(tokenUse)) {
                return new JwtAuthenticationToken(jwt, List.of());
            }
            UUID userId = parseUuid(jwt.getSubject());
            UUID companyId = parseUuid(jwt.getClaimAsString(TenantContextFilter.TENANT_ID_CLAIM));
            UUID membershipId = parseUuid(jwt.getClaimAsString("membership_id"));
            Long version = longClaim(jwt, "authz_version");
            if (userId == null || companyId == null || membershipId == null || version == null) {
                return new JwtAuthenticationToken(jwt, List.of());
            }
            var authorization = accessControlFacade.validateScopedToken(membershipId, userId, companyId, version);
            List<GrantedAuthority> authorities = Stream.concat(
                            authorization.permissions().stream(),
                            authorization.roles().stream().map(role -> "ROLE_" + role.code()))
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .toList();
            return new JwtAuthenticationToken(jwt, authorities);
        };
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

    private AuthorizationManager<RequestAuthorizationContext> tokenUse(String tokenUse) {
        return (authentication, context) -> {
            if (authentication.get() instanceof JwtAuthenticationToken jwtAuthentication
                    && tokenUse.equals(jwtAuthentication.getToken().getClaimAsString("token_use"))
                    && (!"scoped".equals(tokenUse) || hasTenant(jwtAuthentication))) {
                return new AuthorizationDecision(true);
            }
            return new AuthorizationDecision(false);
        };
    }

    @Bean
    TenantContextFilter tenantContextFilter() {
        return new TenantContextFilter();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtEncoder jwtEncoder(IamJwtProperties properties) {
        JWKSource<SecurityContext> source = new ImmutableSecret<>(secretKey(properties));
        return new NimbusJwtEncoder(source);
    }

    @Bean
    JwtDecoder jwtDecoder(IamJwtProperties properties) {
        return NimbusJwtDecoder.withSecretKey(secretKey(properties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    private SecretKey secretKey(IamJwtProperties properties) {
        String secret = properties.getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("erp.iam.jwt.secret must contain at least 32 bytes");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private boolean hasTenant(JwtAuthenticationToken authentication) {
        String tenantId = authentication.getToken().getClaimAsString(TenantContextFilter.TENANT_ID_CLAIM);
        if (!StringUtils.hasText(tenantId)) {
            tenantId = authentication.getToken().getClaimAsString(TenantContextFilter.COMPANY_ID_CLAIM);
        }
        if (!StringUtils.hasText(tenantId)) {
            return false;
        }
        try {
            UUID.fromString(tenantId);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
