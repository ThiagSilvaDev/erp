package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.AuthorizationAuditRepository;

class AuthorizationAuditRetentionTest {

    private final AuthorizationAuditRepository repository = mock(AuthorizationAuditRepository.class);
    private final AuthorizationAuditRetention retention = new AuthorizationAuditRetention(repository, 30L);

    @Test
    void shouldDeleteEntriesOlderThanConfiguredWindow() {
        when(repository.deleteOlderThan(any(Instant.class))).thenReturn(5L);

        retention.purgeExpiredEntries();

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(repository).deleteOlderThan(captor.capture());
        Instant cutoff = captor.getValue();
        Instant expectedWindowStart = Instant.now().minus(Duration.ofDays(30));
        assertThat(cutoff).isBetween(expectedWindowStart.minusSeconds(5), Instant.now());
    }
}
