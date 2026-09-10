package com.bolsillo.ahorro.application.fake;

import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.domain.port.GoalRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryGoalRepository implements GoalRepository {

    private final Map<GoalId, Goal> goals = new LinkedHashMap<>();

    @Override
    public Goal save(Goal goal) {
        goals.put(goal.id(), goal);
        return goal;
    }

    @Override
    public Optional<Goal> findById(GoalId id) {
        return Optional.ofNullable(goals.get(id));
    }

    @Override
    public List<Goal> findAll() {
        return new ArrayList<>(goals.values());
    }
}
