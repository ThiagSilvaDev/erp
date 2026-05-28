package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.UUID;

public record UserCompanyResponse(UUID companyId, String legalName, String tradeName, String cnpj, String role) {}
