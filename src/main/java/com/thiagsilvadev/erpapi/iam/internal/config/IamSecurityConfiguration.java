package com.thiagsilvadev.erpapi.iam.internal.config;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.StringUtils;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AccessControlFacade;
import com.thiagsilvadev.erpapi.iam.internal.repository.IamCredentialRepository;
import com.thiagsilvadev.erpapi.iam.internal.security.CredentialAuthenticationService;
import com.thiagsilvadev.erpapi.iam.internal.security.CustomAccessDeniedHandler;
import com.thiagsilvadev.erpapi.iam.internal.security.CustomAuthenticationEntryPoint;
import com.thiagsilvadev.erpapi.iam.internal.security.IamJwtAuthenticationConverter;
import com.thiagsilvadev.erpapi.iam.internal.security.JwtClaimsValidator;
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
            CredentialAuthenticationService credentials,
            JwtDecoder decoder,
            TenantContextFilter tenantContextFilter) {
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
        provider.setJwtAuthenticationConverter(new IamJwtAuthenticationConverter(accessControlFacade, credentials));
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
                        .access(tokenUse("global"))
                        .requestMatchers("/api/v1/auth/companies")
                        .access(tokenUse("global"))
                        .requestMatchers("/api/v1/auth/select-platform")
                        .access(tokenUse("global"))
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
                .oauth2ResourceServer(oauth2 -> oauth2.authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationManagerResolver(request -> new ProviderManager(provider)))
                .addFilterAfter(tenantContextFilter, BearerTokenAuthenticationFilter.class)
                .build();
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
    CredentialAuthenticationService credentialAuthenticationService(IamCredentialRepository repository) {
        return new CredentialAuthenticationService(repository);
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
    JwtDecoder jwtDecoder(IamJwtProperties properties, Clock clock) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey(properties))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtTimestampValidator timestamps = new JwtTimestampValidator();
        timestamps.setClock(clock);
        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(
                List.of(timestamps, new JwtIssuerValidator(properties.getIssuer()), new JwtClaimsValidator(clock))));
        return decoder;
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
