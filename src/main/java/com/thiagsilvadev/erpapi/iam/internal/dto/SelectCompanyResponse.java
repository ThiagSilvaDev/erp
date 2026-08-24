package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record SelectCompanyResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
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
