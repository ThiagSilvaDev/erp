package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.AuthorizationStatus;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantMembership;

public interface TenantMembershipRepository extends JpaRepository<TenantMembership, UUID> {

    List<TenantMembership> findByUserIdAndStatus(UUID userId, AuthorizationStatus status);

    List<TenantMembership> findByCompanyIdAndStatus(UUID companyId, AuthorizationStatus status);

    List<TenantMembership> findByCompanyIdOrderByCreatedAt(UUID companyId);

    Optional<TenantMembership> findByUserIdAndCompanyId(UUID userId, UUID companyId);

    Optional<TenantMembership> findByIdAndUserIdAndCompanyId(UUID id, UUID userId, UUID companyId);

    Optional<TenantMembership> findByIdAndCompanyId(UUID id, UUID companyId);
}
