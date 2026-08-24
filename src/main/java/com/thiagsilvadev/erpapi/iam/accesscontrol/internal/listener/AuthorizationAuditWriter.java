package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.AuthorizationAudit;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.AuthorizationAuditRepository;

/**
 * Persists {@link AuthorizationAudit} entries in the ambient transaction. The audit listener runs
 * in the {@code BEFORE_COMMIT} phase, so the audit row joins the same transaction as the business
 * change it records: an audit failure rolls the business change back instead of leaving an
 * unaudited security-relevant change committed. When invoked outside any transaction the write
 * opens its own.
 */
@Component
public class AuthorizationAuditWriter {

    private final AuthorizationAuditRepository repository;

    public AuthorizationAuditWriter(AuthorizationAuditRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void write(AuthorizationAudit audit) {
        repository.save(audit);
    }
}
