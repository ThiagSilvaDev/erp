package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "Signed JWT bearer token (token_use=global|scoped|platform)") String accessToken,
        String tokenType,
        @Schema(description = "Token lifetime in seconds", example = "3600") long expiresIn,
        AuthUserResponse user,
        List<AuthCompanyResponse> availableCompanies,
        @Schema(
                        description =
                                "Automatically populated when the user has exactly one active company membership; null otherwise")
                SelectCompanyResponse scopedToken) {
    public LoginResponse {
        tokenType = tokenType == null ? "Bearer" : tokenType;
        availableCompanies = availableCompanies == null ? List.of() : List.copyOf(availableCompanies);
    }

    public static LoginResponse bearer(
            String accessToken,
            long expiresIn,
            AuthUserResponse user,
            List<AuthCompanyResponse> availableCompanies,
            SelectCompanyResponse scopedToken) {
        return new LoginResponse(accessToken, "Bearer", expiresIn, user, availableCompanies, scopedToken);
    }
}
