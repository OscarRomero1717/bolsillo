package com.bolsillo.ahorro.domain.model;

import java.util.Objects;

public record GoalName(String value) {

    public GoalName {
        Objects.requireNonNull(value, "value is required");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
