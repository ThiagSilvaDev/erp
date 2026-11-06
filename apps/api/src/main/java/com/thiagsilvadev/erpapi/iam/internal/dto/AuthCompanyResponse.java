package com.thiagsilvadev.erpapi.iam.internal.dto;

import java.util.List;
import java.util.UUID;

public record AuthCompanyResponse(UUID companyId, String tradeName, String cnpj, List<AuthRoleResponse> roles) {}
