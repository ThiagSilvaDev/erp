package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record SelectCompanyRequest(@NotNull UUID companyId) {}
