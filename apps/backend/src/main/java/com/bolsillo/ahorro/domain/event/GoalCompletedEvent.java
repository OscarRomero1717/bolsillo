package com.bolsillo.ahorro.domain.event;

import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.domain.model.GoalName;
import com.bolsillo.ahorro.domain.model.Money;
import java.time.Instant;

public record GoalCompletedEvent(GoalId goalId, GoalName name, Money targetAmount, Instant occurredAt)
        implements DomainEvent {

    public GoalCompletedEvent(GoalId goalId, GoalName name, Money targetAmount) {
        this(goalId, name, targetAmount, Instant.now());
    }
}
