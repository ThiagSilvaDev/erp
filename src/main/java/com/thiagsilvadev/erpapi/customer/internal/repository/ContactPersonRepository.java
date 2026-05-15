package com.thiagsilvadev.erpapi.customer.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.thiagsilvadev.erpapi.customer.internal.model.ContactPerson;

public interface ContactPersonRepository
        extends JpaRepository<ContactPerson, UUID>, JpaSpecificationExecutor<ContactPerson> {}
