package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateTenantRoleRequest(
        @NotBlank String name,
        String description,
        @NotEmpty @Schema(
                        description =
                                "Grantable tenant permission keys in <module>:<action> format already held by the caller",
                        example = "[\"catalog:read\", \"sales:write\"]")
                Set<@NotBlank String> permissions) {}
