package com.thiagsilvadev.erpapi.user.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserMembershipFacade {

    List<UserMembershipSnapshot> findActiveMemberships(UUID userId);

    Optional<UserMembershipSnapshot> findMembership(UUID userId, UUID companyId);
}
