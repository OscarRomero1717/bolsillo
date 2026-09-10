package com.bolsillo.ahorro.application;

import com.bolsillo.ahorro.domain.exception.GoalNotFoundException;
import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.domain.port.GoalRepository;
import java.util.Objects;

public final class GetGoalUseCase {

    private final GoalRepository goals;

    public GetGoalUseCase(GoalRepository goals) {
        this.goals = Objects.requireNonNull(goals, "goals is required");
    }

    public Goal execute(GoalId id) {
        return goals.findById(id).orElseThrow(() -> new GoalNotFoundException(id));
    }
}
