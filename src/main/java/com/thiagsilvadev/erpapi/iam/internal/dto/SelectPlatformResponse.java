package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.Set;

public record SelectPlatformResponse(
        String accessToken, String tokenType, long expiresIn, String role, Set<String> effectivePermissions) {

    public SelectPlatformResponse {
        tokenType = tokenType == null ? "Bearer" : tokenType;
    }
}
