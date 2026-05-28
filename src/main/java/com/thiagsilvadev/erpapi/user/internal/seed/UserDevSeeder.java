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
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningFacade;
import com.thiagsilvadev.erpapi.user.api.CredentialProvisioningRequest;
import com.thiagsilvadev.erpapi.user.api.UserRole;
import com.thiagsilvadev.erpapi.user.internal.event.UserEventPublisher;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.model.UserMembership;
import com.thiagsilvadev.erpapi.user.internal.repository.UserMembershipRepository;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@Component
@Profile("dev")
@Order(300)
class UserDevSeeder implements ApplicationRunner {

    static final String ADMIN_EMAIL = "admin@acme.example";
    static final String ADMIN_PASSWORD = "ChangeMe123!";
    private static final String ACME_CNPJ = "97.986.234/0001-91";
    private static final String PARTNER_CNPJ = "17.018.282/0001-57";

    private final UserRepository userRepository;
    private final CompanyFacade companyFacade;
    private final UserEventPublisher eventPublisher;
    private final CredentialProvisioningFacade credentialProvisioningFacade;
    private final UserMembershipRepository userMembershipRepository;

    UserDevSeeder(
            UserRepository userRepository,
            CompanyFacade companyFacade,
            UserEventPublisher eventPublisher,
            CredentialProvisioningFacade credentialProvisioningFacade,
            UserMembershipRepository userMembershipRepository) {
        this.userRepository = userRepository;
        this.companyFacade = companyFacade;
        this.eventPublisher = eventPublisher;
        this.credentialProvisioningFacade = credentialProvisioningFacade;
        this.userMembershipRepository = userMembershipRepository;
    }

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        Contact contact = Contact.create(ADMIN_EMAIL, "11987654321");
        CompanySnapshot acme = companyFacade
                .findByCnpj(ACME_CNPJ)
                .orElseThrow(() -> new IllegalStateException("Seed company must exist before seeding users"));
        CompanySnapshot partner = companyFacade
                .findByCnpj(PARTNER_CNPJ)
                .orElseThrow(() -> new IllegalStateException("Seed partner company must exist before seeding users"));

        if (userRepository.existsByContactEmailValue(contact.getEmail())) {
            User user = userRepository
                    .findByContactEmailValue(contact.getEmail())
                    .orElseThrow(() -> new IllegalStateException("Seed user lookup failed after existence check"));
            seedMemberships(user, acme, partner);
            return;
        }

        User user = User.create("Admin", "Acme", contact, LocalDate.of(1990, 1, 1), UserRole.ADMIN, acme.id());
        User savedUser = userRepository.saveAndFlush(user);
        seedMemberships(savedUser, acme, partner);
        credentialProvisioningFacade.provision(new CredentialProvisioningRequest(
                savedUser.getId(),
                savedUser.getContact().getEmail(),
                ADMIN_PASSWORD,
                savedUser.getRole().name()));
        eventPublisher.userCreated(savedUser);
    }

    private void seedMemberships(User user, CompanySnapshot... companies) {
        for (CompanySnapshot company : companies) {
            userMembershipRepository
                    .findByUserIdAndCompanyId(user.getId(), company.id())
                    .ifPresentOrElse(
                            UserMembership::activate,
                            () -> userMembershipRepository.save(
                                    UserMembership.create(user.getId(), company.id(), user.getRole())));
        }
    }
}
