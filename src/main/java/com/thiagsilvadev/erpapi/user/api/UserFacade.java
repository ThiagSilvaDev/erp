package com.thiagsilvadev.erpapi.user.api;

import java.util.UUID;

public interface UserFacade {

    UserSnapshot getUser(UUID userId);

    UserSnapshot resolveUser(String email);

    void requireUser(UUID userId);
}
