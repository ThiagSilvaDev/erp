package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.seed;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service.PlatformAuthorizationService;
import com.thiagsilvadev.erpapi.user.api.UserFacade;

@Component
@Profile("dev")
@Order(400)
class PlatformAdminDevSeeder implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@acme.example";

    private final UserFacade userFacade;
    private final PlatformAuthorizationService platformAuthorizationService;

    PlatformAdminDevSeeder(UserFacade userFacade, PlatformAuthorizationService platformAuthorizationService) {
        this.userFacade = userFacade;
        this.platformAuthorizationService = platformAuthorizationService;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        platformAuthorizationService.ensureAdmin(
                userFacade.resolveUser(ADMIN_EMAIL).id());
    }
}
