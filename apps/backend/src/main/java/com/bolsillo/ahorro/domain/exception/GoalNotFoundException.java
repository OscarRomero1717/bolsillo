package com.bolsillo.ahorro.domain.exception;

import com.bolsillo.ahorro.domain.model.GoalId;

public class GoalNotFoundException extends DomainException {

    public static final String GOAL_NOT_FOUND = "GOAL_NOT_FOUND";

    public GoalNotFoundException(GoalId id) {
        super(GOAL_NOT_FOUND, "Goal not found: " + id);
    }
}
