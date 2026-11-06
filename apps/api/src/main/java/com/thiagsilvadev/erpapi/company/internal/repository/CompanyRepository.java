package com.thiagsilvadev.erpapi.company.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.company.internal.model.Company;

public interface CompanyRepository extends JpaRepository<Company, UUID>, JpaSpecificationExecutor<Company> {

    boolean existsByCnpj(Cnpj cnpj);

    Optional<Company> findByCnpj(Cnpj cnpj);
}
