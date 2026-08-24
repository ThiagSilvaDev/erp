package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationAudit;

public interface AuthorizationAuditRepository extends JpaRepository<AuthorizationAudit, UUID> {

    @Modifying
    @Query("delete from AuthorizationAudit a where a.createdAt < :cutoff")
    long deleteOlderThan(@Param("cutoff") Instant cutoff);
}
