package com.thiagsilvadev.erpapi.user.api;

import java.util.List;
import java.util.UUID;

public interface UserMembershipFacade {

    List<UserMembershipSnapshot> getActiveMemberships(UUID userId);

    UserMembershipSnapshot getActiveMembership(UUID userId, UUID companyId);
}
