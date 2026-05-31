package com.thiagsilvadev.erpapi.iam.internal.security;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;

public class TenantContextFilter extends OncePerRequestFilter {

    public static final String TENANT_ID_CLAIM = "tenant_id";
    public static final String COMPANY_ID_CLAIM = "company_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            resolveTenantId().ifPresent(TenantContext::setTenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private Optional<UUID> resolveTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            return Optional.empty();
        }

        String tenantId = jwtAuthentication.getToken().getClaimAsString(TENANT_ID_CLAIM);
        if (!StringUtils.hasText(tenantId)) {
            tenantId = jwtAuthentication.getToken().getClaimAsString(COMPANY_ID_CLAIM);
        }
        if (!StringUtils.hasText(tenantId)) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(tenantId));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
