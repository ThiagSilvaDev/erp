package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateTenantRoleRequest(
        @NotBlank String name, String description, @NotEmpty Set<@NotBlank String> permissions) {}
