package com.thiagsilvadev.erpapi.iam.internal.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, AuthUserResponse user) {
    public LoginResponse {
        tokenType = tokenType == null ? "Bearer" : tokenType;
    }

    public static LoginResponse bearer(String accessToken, long expiresIn, AuthUserResponse user) {
        return new LoginResponse(accessToken, "Bearer", expiresIn, user);
    }
}
