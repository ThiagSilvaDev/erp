package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.AuthorizationAuditRepository;

/**
 * Purges {@code iam_authorization_audits} entries older than the configured retention window.
 *
 * <p>Enabled by default; set {@code erp.iam.audit.retention.enabled=false} to disable,
 * {@code erp.iam.audit.retention.days} for the retention window (default 365) and
 * {@code erp.iam.audit.retention.cron} for the schedule (default daily at 03:00).</p>
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "erp.iam.audit.retention.enabled", havingValue = "true", matchIfMissing = true)
public class AuthorizationAuditRetention {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationAuditRetention.class);

    private final AuthorizationAuditRepository repository;
    private final Duration retention;

    public AuthorizationAuditRetention(
            AuthorizationAuditRepository repository, @Value("${erp.iam.audit.retention.days:365}") long retentionDays) {
        this.repository = repository;
        this.retention = Duration.ofDays(retentionDays);
    }

    @Scheduled(cron = "${erp.iam.audit.retention.cron:0 0 3 * * *}")
    @Transactional
    public void purgeExpiredEntries() {
        Instant cutoff = Instant.now().minus(retention);
        long deleted = repository.deleteOlderThan(cutoff);
        if (deleted > 0) {
            logger.info("Purged {} authorization audit entries older than {}.", deleted, cutoff);
        }
    }
}
