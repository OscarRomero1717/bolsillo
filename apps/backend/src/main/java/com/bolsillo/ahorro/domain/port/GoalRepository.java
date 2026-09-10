package com.bolsillo.ahorro.domain.port;

import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.GoalId;
import java.util.List;
import java.util.Optional;

public interface GoalRepository {

    Goal save(Goal goal);

    Optional<Goal> findById(GoalId id);

    List<Goal> findAll();
}
