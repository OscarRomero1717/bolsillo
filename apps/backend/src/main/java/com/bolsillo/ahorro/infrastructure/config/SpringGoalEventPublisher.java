package com.bolsillo.ahorro.infrastructure.config;

import com.bolsillo.ahorro.domain.event.DomainEvent;
import com.bolsillo.ahorro.domain.port.GoalEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringGoalEventPublisher implements GoalEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringGoalEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
