package com.bolsillo.ahorro.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount) {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public Money {
        Objects.requireNonNull(amount, "amount is required");
        amount = amount.setScale(SCALE, ROUNDING);
    }

    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }

    public Money plus(Money other) {
        Objects.requireNonNull(other, "other is required");
        return new Money(amount.add(other.amount));
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public int compareTo(Money other) {
        Objects.requireNonNull(other, "other is required");
        return amount.compareTo(other.amount);
    }
}
