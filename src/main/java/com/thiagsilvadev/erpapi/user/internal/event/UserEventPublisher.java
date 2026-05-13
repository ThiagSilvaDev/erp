package com.thiagsilvadev.erpapi.user.internal.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.thiagsilvadev.erpapi.user.api.UserCreatedEvent;
import com.thiagsilvadev.erpapi.user.internal.model.User;

@Component
public class UserEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(UserEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public UserEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void userCreated(User user) {
        UserCreatedEvent event =
                new UserCreatedEvent(user.getId(), user.getContact().getEmail(), user.getCompanyId());

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
            logger.error("Failed to publish user event {}", event, exception);
        }
    }
}
