package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.AuthorizationAuditRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuthorizationAuditJpaTest.AuditingConfiguration.class)
@ActiveProfiles("dbtest")
class AuthorizationAuditJpaTest {

    @Autowired
    private AuthorizationAuditRepository repository;

    @Test
    void shouldPersistAuditWithPayloads() {
        UUID companyId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        AuthorizationAudit audit = AuthorizationAudit.create(
                companyId,
                null,
                AuthorizationActorType.UNKNOWN,
                AuthorizationTargetType.ROLE,
                targetId,
                AuthorizationAction.ROLE_CREATED,
                "{\"before\":[]}",
                "{\"code\":\"CUSTOM_1\",\"permissions\":[\"catalog:read\"]}");

        AuthorizationAudit saved = repository.saveAndFlush(audit);

        assertThat(saved.getId()).isNotNull();
        AuthorizationAudit loaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getAction()).isEqualTo(AuthorizationAction.ROLE_CREATED);
        assertThat(loaded.getTargetId()).isEqualTo(targetId);
        assertThat(loaded.getBeforeValue()).isEqualTo("{\"before\":[]}");
        assertThat(loaded.getAfterValue()).contains("CUSTOM_1").contains("catalog:read");
    }

    @EnableJpaAuditing
    static class AuditingConfiguration {}
}
