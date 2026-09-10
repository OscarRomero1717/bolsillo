package com.bolsillo.ahorro.domain.model;

import java.time.Instant;
import java.util.Objects;

public record Contribution(Money amount, Instant at) {

    public Contribution {
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(at, "at is required");
    }
}
