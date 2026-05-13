package com.thiagsilvadev.erpapi.iam.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.iam.internal.model.IamCredential;

public interface IamCredentialRepository extends JpaRepository<IamCredential, UUID> {

    boolean existsByUserId(UUID userId);

    boolean existsByEmailValue(String email);

    Optional<IamCredential> findByEmailValue(String email);
}
