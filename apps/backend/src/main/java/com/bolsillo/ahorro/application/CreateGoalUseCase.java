package com.bolsillo.ahorro.application;

import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.Money;
import com.bolsillo.ahorro.domain.port.GoalRepository;
import java.util.Objects;

public final class CreateGoalUseCase {

    private final GoalRepository goals;

    public CreateGoalUseCase(GoalRepository goals) {
        this.goals = Objects.requireNonNull(goals, "goals is required");
    }

    public Goal execute(String name, Money targetAmount) {
        Goal created = Goal.create(name, targetAmount);
        return goals.save(created);
    }
}
