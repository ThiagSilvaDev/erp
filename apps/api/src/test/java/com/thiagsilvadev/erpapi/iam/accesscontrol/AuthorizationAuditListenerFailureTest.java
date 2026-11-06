package com.thiagsilvadev.erpapi.iam.accesscontrol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantMembershipProvisionedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.listener.AuthorizationAuditWriter;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationAudit;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.AuthorizationAuditRepository;

@SpringBootTest
@ActiveProfiles("dbtest")
class AuthorizationAuditListenerFailureTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private AuthorizationAuditRepository auditRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private AuthorizationAuditWriter writer;

    @Test
    void shouldRollBackBusinessTransactionWhenAuditWriteFails() {
        UUID membershipId = UUID.randomUUID();
        doThrow(new RuntimeException("audit store unavailable")).when(writer).write(any(AuthorizationAudit.class));

        assertThatThrownBy(() -> new TransactionTemplate(transactionManager)
                        .executeWithoutResult(
                                status -> eventPublisher.publishEvent(new TenantMembershipProvisionedEvent(
                                        UUID.randomUUID(),
                                        membershipId,
                                        UUID.randomUUID(),
                                        Set.of(UUID.randomUUID()),
                                        null))))
                .hasMessage("audit store unavailable");

        assertThat(auditRepository.findAll())
                .noneMatch(audit -> audit.getTargetId().equals(membershipId));
    }
}
