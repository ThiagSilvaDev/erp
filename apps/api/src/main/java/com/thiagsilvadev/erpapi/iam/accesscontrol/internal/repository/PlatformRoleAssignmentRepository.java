package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.PlatformRoleAssignment;

public interface PlatformRoleAssignmentRepository extends JpaRepository<PlatformRoleAssignment, UUID> {

    Optional<PlatformRoleAssignment> findByUserId(UUID userId);
}
