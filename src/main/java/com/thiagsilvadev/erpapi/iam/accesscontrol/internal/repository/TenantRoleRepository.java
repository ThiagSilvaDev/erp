package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;

public interface TenantRoleRepository extends JpaRepository<TenantRole, UUID> {

    List<TenantRole> findByCompanyIdAndStatusOrderByName(UUID companyId, AuthorizationStatus status);

    Optional<TenantRole> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<TenantRole> findByCompanyIdAndCode(UUID companyId, String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from TenantRole r where r.companyId = :companyId and r.code = :code")
    Optional<TenantRole> findByCompanyIdAndCodeForUpdate(
            @Param("companyId") UUID companyId, @Param("code") String code);
}
