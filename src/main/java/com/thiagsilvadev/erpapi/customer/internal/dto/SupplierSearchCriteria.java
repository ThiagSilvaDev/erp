package com.thiagsilvadev.erpapi.customer.internal.dto;

import java.util.UUID;

import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

public record SupplierSearchCriteria(
        @Size(max = 120) String name,
        @Size(max = 32) @Schema(
                        description =
                                "Tax identification document (CPF/CNPJ); non-digits are stripped, up to 32 digits",
                        example = "12345678909")
                String document,
        UUID companyId) {}
