package com.bolsillo.ahorro.interfaces.rest;

import com.bolsillo.ahorro.domain.model.Goal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record GoalResponse(
        String id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        int progressPercent,
        String status,
        long version) {

    public static GoalResponse from(Goal goal) {
        Objects.requireNonNull(goal, "goal is required");
        return new GoalResponse(
                goal.id().toString(),
                goal.name().value(),
                goal.targetAmount().amount(),
                goal.currentAmount().amount(),
                progressPercent(goal),
                goal.status().name(),
                goal.version());
    }

    private static int progressPercent(Goal goal) {
        BigDecimal target = goal.targetAmount().amount();
        if (target.signum() == 0) {
            return 0;
        }
        return goal.currentAmount()
                .amount()
                .multiply(BigDecimal.valueOf(100))
                .divide(target, 0, RoundingMode.HALF_UP)
                .intValue();
    }
}
