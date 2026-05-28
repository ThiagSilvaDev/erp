package com.thiagsilvadev.erpapi.user.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagsilvadev.erpapi.user.api.UserMembershipStatus;
import com.thiagsilvadev.erpapi.user.internal.model.UserMembership;

public interface UserMembershipRepository extends JpaRepository<UserMembership, UUID> {

    List<UserMembership> findByUserIdAndStatus(UUID userId, UserMembershipStatus status);

    Optional<UserMembership> findByUserIdAndCompanyId(UUID userId, UUID companyId);
}
