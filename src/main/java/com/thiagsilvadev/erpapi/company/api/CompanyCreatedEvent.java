package com.thiagsilvadev.erpapi.company.api;

import java.util.UUID;

public record CompanyCreatedEvent(UUID companyId, String cnpj) {

    public static CompanyCreatedEvent created(UUID companyId, String cnpj) {
        return new CompanyCreatedEvent(companyId, cnpj);
    }
}
