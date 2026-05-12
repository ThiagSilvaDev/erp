package com.thiagsilvadev.erpapi.company.api;

import java.util.Optional;
import java.util.UUID;

public interface CompanyFacade {

    Optional<CompanySnapshot> findById(UUID id);

    Optional<CompanySnapshot> findByCnpj(String cnpj);
}
