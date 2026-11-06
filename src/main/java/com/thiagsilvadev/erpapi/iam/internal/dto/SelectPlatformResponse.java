package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

public record SelectPlatformResponse(
        @Schema(description = "Signed JWT bearer token (token_use=global|scoped|platform)") String accessToken,
        String tokenType,
        @Schema(description = "Token lifetime in seconds", example = "3600") long expiresIn,
        String role,
        Set<String> effectivePermissions) {

    public SelectPlatformResponse {
        tokenType = tokenType == null ? "Bearer" : tokenType;
    }
}
