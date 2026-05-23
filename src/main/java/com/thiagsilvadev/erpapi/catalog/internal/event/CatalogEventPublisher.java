package com.thiagsilvadev.erpapi.catalog.internal.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.thiagsilvadev.erpapi.catalog.api.ProductCreatedEvent;
import com.thiagsilvadev.erpapi.catalog.internal.model.Product;

@Component
public class CatalogEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(CatalogEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public CatalogEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void productCreated(Product product) {
        ProductCreatedEvent event = ProductCreatedEvent.created(product.getId(), product.getSku());

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
            logger.error("Failed to publish catalog event {}", event, exception);
        }
    }
}
