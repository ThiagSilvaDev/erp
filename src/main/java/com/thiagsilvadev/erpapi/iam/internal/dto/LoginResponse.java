package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        AuthUserResponse user,
        List<AuthCompanyResponse> availableCompanies,
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
