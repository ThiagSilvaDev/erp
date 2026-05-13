package com.thiagsilvadev.erpapi.user.api;

import java.util.Optional;
import java.util.UUID;

public interface UserFacade {

    Optional<UserSnapshot> findById(UUID id);

    Optional<UserSnapshot> findByEmail(String email);
}
