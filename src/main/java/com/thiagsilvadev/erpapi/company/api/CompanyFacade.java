package com.thiagsilvadev.erpapi.company.api;

import java.util.UUID;

public interface CompanyFacade {

    CompanySnapshot getCompany(UUID companyId);

    CompanySnapshot resolveCompany(String cnpj);
}
