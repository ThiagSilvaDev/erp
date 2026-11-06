package com.thiagsilvadev.erpapi.company.internal.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.common.transaction.AfterCommit;
import com.thiagsilvadev.erpapi.company.api.CompanyCreatedEvent;
import com.thiagsilvadev.erpapi.company.internal.model.Company;

@Component
public class CompanyEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(CompanyEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;
    private final AuditActorResolver actorResolver;

    public CompanyEventPublisher(ApplicationEventPublisher eventPublisher, AuditActorResolver actorResolver) {
        this.eventPublisher = eventPublisher;
        this.actorResolver = actorResolver;
    }

    public void companyCreated(Company company) {
        CompanyCreatedEvent event =
                new CompanyCreatedEvent(company.getId(), company.getCnpj().getNumber(), actorResolver.userIdOrNull());

        AfterCommit.run(() -> publish(event));
    }

    private void publish(Object event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (RuntimeException exception) {
            logger.error("Failed to publish company event {}", event, exception);
        }
    }
}
