package com.thiagsilvadev.erpapi.iam.internal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;

class TenantContextFilterTest {
    @AfterEach
    void clear() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void clearsContextAfterFailure() {
        var id = UUID.randomUUID();
        var jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("token_use", "scoped")
                .claim("tenant_id", id.toString())
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
        var filter = new TenantContextFilter();
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        assertThatThrownBy(() -> filter.doFilter(request, response, (req, res) -> {
                    assertThat(TenantContext.requireTenantId()).isEqualTo(id);
                    throw new ServletException("failure");
                }))
                .isInstanceOf(ServletException.class);
        assertThatThrownBy(TenantContext::requireTenantId).isInstanceOf(RuntimeException.class);
    }
}
