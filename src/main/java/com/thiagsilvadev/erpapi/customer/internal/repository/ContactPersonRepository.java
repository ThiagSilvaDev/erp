package com.thiagsilvadev.erpapi.customer.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.customer.internal.model.ContactPerson;

public interface ContactPersonRepository
        extends JpaRepository<ContactPerson, UUID>, JpaSpecificationExecutor<ContactPerson> {

    @Query(
            """
            select contact
            from ContactPerson contact
            join Customer customer on customer.id = contact.customerId
            where contact.id = :id and customer.ownerCompanyId = :ownerCompanyId
            """)
    Optional<ContactPerson> findByIdAndCustomerOwnerCompanyId(
            @Param("id") UUID id, @Param("ownerCompanyId") UUID ownerCompanyId);

    @Query(
            """
            select contact
            from ContactPerson contact
            join Customer customer on customer.id = contact.customerId
            where customer.ownerCompanyId = :ownerCompanyId
            """)
    Page<ContactPerson> findAllByCustomerOwnerCompanyId(
            @Param("ownerCompanyId") UUID ownerCompanyId, Pageable pageable);
}
