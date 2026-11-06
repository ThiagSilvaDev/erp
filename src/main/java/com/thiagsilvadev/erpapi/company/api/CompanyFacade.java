package com.thiagsilvadev.erpapi.company.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CompanyFacade {

    CompanySnapshot getCompany(UUID companyId);

    List<CompanySnapshot> getCompanies(Set<UUID> companyIds);

    CompanySnapshot resolveCompany(String cnpj);
}
