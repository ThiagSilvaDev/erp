package com.thiagsilvadev.erpapi.customer.internal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

public record ContactRequest(
        @NotBlank @Email String email,
        @Schema(
                        description =
                                "Optional Brazilian phone number; non-digits are stripped and must yield 10 or 11 digits",
                        example = "11987654321")
                String phone) {}
