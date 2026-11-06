package com.thiagsilvadev.erpapi.company.internal.dto;

import jakarta.validation.constraints.Email;

import io.swagger.v3.oas.annotations.media.Schema;

public record ContactRequest(
        @Email String email,
        @Schema(
                        description =
                                "Optional Brazilian phone number; non-digits are stripped and must yield 10 or 11 digits",
                        example = "11987654321")
                String phone) {}
