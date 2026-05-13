package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.UUID;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, UUID userId, String role) {}
