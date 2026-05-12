package com.thiagsilvadev.erpapi.company.internal.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.thiagsilvadev.erpapi.company.api.CompanyCreatedEvent;
import com.thiagsilvadev.erpapi.company.internal.model.Company;

@Component
public class CompanyEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(CompanyEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public CompanyEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void companyCreated(Company company) {
        CompanyCreatedEvent event =
                new CompanyCreatedEvent(company.getId(), company.getCnpj().getNumber());

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publish(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(event);
            }
        });
    }

    private void publish(Object event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (RuntimeException exception) {
            logger.error("Failed to publish company event {}", event, exception);
        }
    }
}
