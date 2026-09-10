package com.bolsillo.ahorro.domain.model;

import java.util.Objects;
import java.util.UUID;

public record GoalId(UUID value) {

    public GoalId {
        Objects.requireNonNull(value, "value is required");
    }

    public static GoalId newId() {
        return new GoalId(UUID.randomUUID());
    }

    public static GoalId from(String raw) {
        Objects.requireNonNull(raw, "raw is required");
        return new GoalId(UUID.fromString(raw));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
