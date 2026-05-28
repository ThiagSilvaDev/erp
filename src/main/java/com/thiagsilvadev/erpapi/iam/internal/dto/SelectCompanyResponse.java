package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.UUID;

import com.thiagsilvadev.erpapi.user.api.UserRole;

public record SelectCompanyResponse(
        String accessToken, String tokenType, long expiresIn, UUID activeCompanyId, UserRole role) {
    public SelectCompanyResponse {
        tokenType = tokenType == null ? "Bearer" : tokenType;
    }

    public static SelectCompanyResponse bearer(
            String accessToken, long expiresIn, UUID activeCompanyId, UserRole role) {
        return new SelectCompanyResponse(accessToken, "Bearer", expiresIn, activeCompanyId, role);
    }
}
