package com.bolsillo.ahorro.domain.port;

import com.bolsillo.ahorro.domain.event.DomainEvent;

public interface GoalEventPublisher {

    void publish(DomainEvent event);
}
