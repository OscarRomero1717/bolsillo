package com.bolsillo.ahorro.application;

import com.bolsillo.ahorro.domain.event.DomainEvent;
import com.bolsillo.ahorro.domain.exception.GoalNotFoundException;
import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.domain.model.Money;
import com.bolsillo.ahorro.domain.port.GoalEventPublisher;
import com.bolsillo.ahorro.domain.port.GoalRepository;
import java.util.List;
import java.util.Objects;

public final class DepositContributionUseCase {

    private final GoalRepository goals;
    private final GoalEventPublisher publisher;

    public DepositContributionUseCase(GoalRepository goals, GoalEventPublisher publisher) {
        this.goals = Objects.requireNonNull(goals, "goals is required");
        this.publisher = Objects.requireNonNull(publisher, "publisher is required");
    }

    public Goal execute(GoalId id, Money amount) {
        Goal goal = goals.findById(id).orElseThrow(() -> new GoalNotFoundException(id));
        List<DomainEvent> events = goal.contribute(amount);
        goals.save(goal);
        for (DomainEvent event : events) {
            publisher.publish(event);
        }
        return goal;
    }
}
