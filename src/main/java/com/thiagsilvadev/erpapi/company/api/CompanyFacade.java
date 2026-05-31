package com.thiagsilvadev.erpapi.company.api;

import java.util.Optional;
import java.util.UUID;

public interface CompanyFacade {

    Optional<CompanySnapshot> findById(UUID id);

    void requireCompanyExistsById(UUID id);

    Optional<CompanySnapshot> findByCnpj(String cnpj);

    Optional<CompanyAddressSnapshot> findAddressByCompanyId(UUID id);
}
