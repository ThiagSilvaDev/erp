package com.thiagsilvadev.erpapi.user.internal.seed;

import java.time.LocalDate;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.user.api.TenantAccessProvisioningFacade;
import com.thiagsilvadev.erpapi.user.internal.event.UserCredentialProvisioningAfterCommitHandler;
import com.thiagsilvadev.erpapi.user.internal.event.UserEventPublisher;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@Component
@Profile("dev")
@Order(300)
class UserDevSeeder implements ApplicationRunner {

    static final String ADMIN_EMAIL = "admin@acme.example";
    static final String ADMIN_PASSWORD = "ChangeMe123!";
    static final String DRIVER_EMAIL = "alex.driver@acme.example";
    private static final String ACME_CNPJ = "97.986.234/0001-91";
    private static final String PARTNER_CNPJ = "17.018.282/0001-57";
    private static final String SEED_PASSWORD = "ChangeMe123!";

    private final UserRepository userRepository;
    private final CompanyFacade companyFacade;
    private final UserEventPublisher eventPublisher;
    private final UserCredentialProvisioningAfterCommitHandler credentialProvisioningAfterCommitHandler;
    private final TenantAccessProvisioningFacade tenantAccessProvisioningFacade;

    UserDevSeeder(
            UserRepository userRepository,
            CompanyFacade companyFacade,
            UserEventPublisher eventPublisher,
            UserCredentialProvisioningAfterCommitHandler credentialProvisioningAfterCommitHandler,
            TenantAccessProvisioningFacade tenantAccessProvisioningFacade) {
        this.userRepository = userRepository;
        this.companyFacade = companyFacade;
        this.eventPublisher = eventPublisher;
        this.credentialProvisioningAfterCommitHandler = credentialProvisioningAfterCommitHandler;
        this.tenantAccessProvisioningFacade = tenantAccessProvisioningFacade;
    }

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        CompanySnapshot acme = companyFacade.resolveCompany(ACME_CNPJ);
        CompanySnapshot partner = companyFacade.resolveCompany(PARTNER_CNPJ);

        seedUser("Admin", "Acme", ADMIN_EMAIL, LocalDate.of(1990, 1, 1), "OWNER", acme, partner);
        seedUser("Mariana", "Silva", "mariana@acme.example", LocalDate.of(1992, 5, 15), "MANAGER", acme, partner);
        seedUser("Carlos", "Oliveira", "carlos@acme.example", LocalDate.of(1988, 11, 3), "OPERATOR", acme, partner);
        seedUser("Juliana", "Santos", "juliana@partner.example", LocalDate.of(1995, 8, 22), "MANAGER", partner, acme);
        seedUser("Bruno", "Costa", "bruno@partner.example", LocalDate.of(1993, 3, 10), "OPERATOR", partner, acme);
        seedUser("Alex", "Driver", DRIVER_EMAIL, LocalDate.of(1991, 6, 12), "DRIVER", acme);
    }

    private void seedUser(
            String firstName,
            String lastName,
            String email,
            LocalDate birthDate,
            String roleCode,
            CompanySnapshot primaryCompany,
            CompanySnapshot... otherCompanies) {
        if (userRepository.existsByContactEmailValue(email)) {
            User user = userRepository
                    .findByContactEmailValue(email)
                    .orElseThrow(() -> new IllegalStateException("Seed user lookup failed after existence check"));
            seedMemberships(user, roleCode, primaryCompany);
            seedMemberships(user, roleCode, otherCompanies);
            return;
        }

        Contact contact = Contact.create(email, "11987654321");
        User user = User.create(firstName, lastName, contact, birthDate);
        User savedUser = userRepository.saveAndFlush(user);
        seedMemberships(savedUser, roleCode, primaryCompany);
        seedMemberships(savedUser, roleCode, otherCompanies);
        credentialProvisioningAfterCommitHandler.provisionAfterCommit(savedUser, SEED_PASSWORD);
        eventPublisher.userCreated(savedUser);
    }

    private void seedMemberships(User user, String roleCode, CompanySnapshot... companies) {
        for (CompanySnapshot company : companies) {
            tenantAccessProvisioningFacade.provisionBuiltIn(user.getId(), company.id(), roleCode);
        }
    }
}
