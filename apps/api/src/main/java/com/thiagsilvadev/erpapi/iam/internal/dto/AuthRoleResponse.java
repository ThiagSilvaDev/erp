package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.UUID;

public record AuthRoleResponse(UUID id, String code, String name) {}
