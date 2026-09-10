package com.bolsillo.ahorro.application;

import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.port.GoalRepository;
import java.util.List;
import java.util.Objects;

public final class ListGoalsUseCase {

    private final GoalRepository goals;

    public ListGoalsUseCase(GoalRepository goals) {
        this.goals = Objects.requireNonNull(goals, "goals is required");
    }

    public List<Goal> execute() {
        return goals.findAll();
    }
}
