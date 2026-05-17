package com.thiagsilvadev.erpapi.iam.internal.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.thiagsilvadev.erpapi.catalog.api.CatalogPermissions;
import com.thiagsilvadev.erpapi.company.api.CompanyPermissions;
import com.thiagsilvadev.erpapi.customer.api.CustomerPermissions;
import com.thiagsilvadev.erpapi.financial.api.FinancialPermissions;
import com.thiagsilvadev.erpapi.iam.internal.security.CustomAccessDeniedHandler;
import com.thiagsilvadev.erpapi.iam.internal.security.CustomAuthenticationEntryPoint;
import com.thiagsilvadev.erpapi.iam.internal.security.RolePermissionResolver;
import com.thiagsilvadev.erpapi.iam.internal.security.SecurityAuthorities;
import com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions;
import com.thiagsilvadev.erpapi.sales.api.SalesPermissions;
import com.thiagsilvadev.erpapi.user.api.UserPermissions;

@Configuration
@EnableConfigurationProperties(IamJwtProperties.class)
public class IamSecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler,
            RolePermissionResolver rolePermissionResolver) {
        return http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/catalog/**")
                        .hasAuthority(CatalogPermissions.READ)
                        .requestMatchers("/api/v1/catalog/**")
                        .hasAuthority(CatalogPermissions.WRITE)
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventory/**")
                        .hasAuthority(InventoryPermissions.READ)
                        .requestMatchers("/api/v1/inventory/**")
                        .hasAuthority(InventoryPermissions.WRITE)
                        .requestMatchers(HttpMethod.GET, "/api/v1/financial/**")
                        .hasAuthority(FinancialPermissions.READ)
                        .requestMatchers("/api/v1/financial/**")
                        .hasAuthority(FinancialPermissions.WRITE)
                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/**")
                        .hasAuthority(CompanyPermissions.READ)
                        .requestMatchers("/api/v1/companies/**")
                        .hasAuthority(CompanyPermissions.WRITE)
                        .requestMatchers(HttpMethod.GET, "/api/v1/customers/**", "/api/v1/suppliers/**", "/api/v1/contact-persons/**")
                        .hasAuthority(CustomerPermissions.READ)
                        .requestMatchers("/api/v1/customers/**", "/api/v1/suppliers/**", "/api/v1/contact-persons/**")
                        .hasAuthority(CustomerPermissions.WRITE)
                        .requestMatchers(HttpMethod.GET, "/api/v1/sales/**")
                        .hasAuthority(SalesPermissions.READ)
                        .requestMatchers("/api/v1/sales/**")
                        .hasAuthority(SalesPermissions.WRITE)
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**")
                        .hasAuthority(UserPermissions.READ)
                        .requestMatchers("/api/v1/users/**")
                        .hasAuthority(UserPermissions.WRITE)
                        .requestMatchers(HttpMethod.GET, "/api/v1/**")
                        .authenticated()
                        .requestMatchers("/api/v1/**")
                        .denyAll()
                        .anyRequest()
                        .permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter(rolePermissionResolver))))
                .build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter(RolePermissionResolver rolePermissionResolver) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> authorities = jwt.getClaimAsStringList("authorities");
            if (authorities != null && !authorities.isEmpty()) {
                return authorities.stream()
                        .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                        .toList();
            }
            String role = jwt.getClaimAsString("role");
            return SecurityAuthorities.fromRole(role, rolePermissionResolver);
        });
        return converter;
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
}
