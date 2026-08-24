package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.List;
import java.util.UUID;

public record UserCompanyResponse(
        UUID companyId, String legalName, String tradeName, String cnpj, List<AuthRoleResponse> roles) {}
