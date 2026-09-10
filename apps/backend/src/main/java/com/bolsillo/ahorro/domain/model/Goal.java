package com.bolsillo.ahorro.domain.model;

import com.bolsillo.ahorro.domain.event.DomainEvent;
import com.bolsillo.ahorro.domain.event.GoalCompletedEvent;
import com.bolsillo.ahorro.domain.event.GoalUpdatedEvent;
import com.bolsillo.ahorro.domain.exception.ContributionNotAllowedException;
import com.bolsillo.ahorro.domain.exception.DomainException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Goal {

    public static final String TARGET_NOT_POSITIVE = "TARGET_NOT_POSITIVE";

    private final GoalId id;
    private final GoalName name;
    private final Money targetAmount;
    private Money currentAmount;
    private GoalStatus status;
    private long version;

    private Goal(
            GoalId id,
            GoalName name,
            Money targetAmount,
            Money currentAmount,
            GoalStatus status,
            long version) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.status = status;
        this.version = version;
    }

    public static Goal create(String name, Money targetAmount) {
        Objects.requireNonNull(targetAmount, "targetAmount is required");
        if (!targetAmount.isPositive()) {
            throw new DomainException(TARGET_NOT_POSITIVE, "Target amount must be greater than zero");
        }
        return new Goal(
                GoalId.newId(),
                new GoalName(name),
                targetAmount,
                Money.of("0"),
                GoalStatus.OPEN,
                0L);
    }

    public static Goal rehydrate(
            GoalId id,
            GoalName name,
            Money targetAmount,
            Money currentAmount,
            GoalStatus status,
            long version) {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(targetAmount, "targetAmount is required");
        Objects.requireNonNull(currentAmount, "currentAmount is required");
        Objects.requireNonNull(status, "status is required");
        return new Goal(id, name, targetAmount, currentAmount, status, version);
    }

    public GoalId id() {
        return id;
    }

    public GoalName name() {
        return name;
    }

    public Money targetAmount() {
        return targetAmount;
    }

    public Money currentAmount() {
        return currentAmount;
    }

    public GoalStatus status() {
        return status;
    }

    public long version() {
        return version;
    }

    public List<DomainEvent> contribute(Money amount) {
        Objects.requireNonNull(amount, "amount is required");
        if (!amount.isPositive()) {
            throw ContributionNotAllowedException.amountNotPositive();
        }
        if (status == GoalStatus.COMPLETED) {
            throw ContributionNotAllowedException.goalAlreadyCompleted();
        }
        Money next = currentAmount.plus(amount);
        if (next.compareTo(targetAmount) > 0) {
            throw ContributionNotAllowedException.exceedsRemaining();
        }

        currentAmount = next;
        if (currentAmount.compareTo(targetAmount) == 0) {
            status = GoalStatus.COMPLETED;
        }

        List<DomainEvent> events = new ArrayList<>();
        events.add(new GoalUpdatedEvent(id, currentAmount, targetAmount, status));
        if (status == GoalStatus.COMPLETED) {
            events.add(new GoalCompletedEvent(id, name, targetAmount));
        }
        return List.copyOf(events);
    }
}
