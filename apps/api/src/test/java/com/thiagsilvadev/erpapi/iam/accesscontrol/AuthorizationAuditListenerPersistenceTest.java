package com.thiagsilvadev.erpapi.iam.accesscontrol;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipProvisionedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.AuthorizationAuditRepository;

@SpringBootTest
@ActiveProfiles("dbtest")
class AuthorizationAuditListenerPersistenceTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private AuthorizationAuditRepository auditRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldPersistAuditRowWhenEventIsPublishedInTransaction() {
        UUID membershipId = UUID.randomUUID();
        new TransactionTemplate(transactionManager)
                .executeWithoutResult(status -> eventPublisher.publishEvent(new TenantMembershipProvisionedEvent(
                        UUID.randomUUID(), membershipId, UUID.randomUUID(), Set.of(UUID.randomUUID()), null)));

        assertThat(auditRepository.findAll())
                .anyMatch(audit -> audit.getTargetId().equals(membershipId));
    }
}
