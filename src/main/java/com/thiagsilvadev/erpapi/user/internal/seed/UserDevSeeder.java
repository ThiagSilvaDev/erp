package com.thiagsilvadev.erpapi.user.internal.seed;

import java.time.LocalDate;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.model.Contact;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.user.api.UserRole;
import com.thiagsilvadev.erpapi.user.internal.event.UserEventPublisher;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@Component
@Profile("dev")
@Order(300)
class UserDevSeeder implements ApplicationRunner {

    static final String ADMIN_EMAIL = "admin@acme.example";
    static final String ADMIN_PASSWORD = "ChangeMe123!";
    private static final String ACME_CNPJ = "11.222.333/0001-81";

    private final UserRepository userRepository;
    private final CompanyFacade companyFacade;
    private final UserEventPublisher eventPublisher;

    UserDevSeeder(UserRepository userRepository, CompanyFacade companyFacade, UserEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.companyFacade = companyFacade;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Contact contact = Contact.create(ADMIN_EMAIL, "11987654321");
        if (userRepository.existsByContactEmailValue(contact.getEmail())) {
            return;
        }

        CompanySnapshot company = companyFacade
                .findByCnpj(ACME_CNPJ)
                .orElseThrow(() -> new IllegalStateException("Seed company must exist before seeding users"));

        User user = User.create("Admin", "Acme", contact, LocalDate.of(1990, 1, 1), UserRole.ADMIN, company.id());
        User savedUser = userRepository.save(user);
        eventPublisher.userCreated(savedUser, ADMIN_PASSWORD);
    }
}
