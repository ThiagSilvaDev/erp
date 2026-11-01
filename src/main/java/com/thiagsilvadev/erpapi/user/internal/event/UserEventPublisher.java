package com.thiagsilvadev.erpapi.user.internal.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.common.transaction.AfterCommit;
import com.thiagsilvadev.erpapi.user.api.UserCreatedEvent;
import com.thiagsilvadev.erpapi.user.internal.model.User;

@Component
public class UserEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public UserEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void userCreated(User user) {
        UserCreatedEvent event =
                new UserCreatedEvent(user.getId(), user.getContact().getEmail());

        AfterCommit.run(() -> eventPublisher.publishEvent(event));
    }
}
