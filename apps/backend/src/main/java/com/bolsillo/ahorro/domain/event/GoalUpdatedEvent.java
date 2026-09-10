package com.bolsillo.ahorro.domain.event;

import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.domain.model.GoalStatus;
import com.bolsillo.ahorro.domain.model.Money;
import java.time.Instant;

public record GoalUpdatedEvent(
        GoalId goalId,
        Money currentAmount,
        Money targetAmount,
        GoalStatus status,
        Instant occurredAt)
        implements DomainEvent {

    public GoalUpdatedEvent(GoalId goalId, Money currentAmount, Money targetAmount, GoalStatus status) {
        this(goalId, currentAmount, targetAmount, status, Instant.now());
    }
}
