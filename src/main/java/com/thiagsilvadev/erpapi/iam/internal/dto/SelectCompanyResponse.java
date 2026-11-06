package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record SelectCompanyResponse(
        @Schema(description = "Signed JWT bearer token (token_use=global|scoped|platform)") String accessToken,
        String tokenType,
        @Schema(description = "Token lifetime in seconds", example = "3600") long expiresIn,
        UUID activeCompanyId,
        List<AuthRoleResponse> roles,
        Set<String> effectivePermissions) {
    public SelectCompanyResponse {
        tokenType = tokenType == null ? "Bearer" : tokenType;
    }

    public static SelectCompanyResponse bearer(
            String accessToken,
            long expiresIn,
            UUID activeCompanyId,
            List<AuthRoleResponse> roles,
            Set<String> effectivePermissions) {
        return new SelectCompanyResponse(
                accessToken, "Bearer", expiresIn, activeCompanyId, roles, effectivePermissions);
    }
}
