package com.thiagsilvadev.erpapi.company.api;

import java.util.UUID;

public record CompanyCreatedEvent(UUID companyId, String cnpj) {}
