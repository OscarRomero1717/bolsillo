package com.bolsillo.ahorro.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void plusAddsScaledAmounts() {
        Money left = new Money(new BigDecimal("10.1"));
        Money right = new Money(new BigDecimal("0.2"));

        Money sum = left.plus(right);

        assertThat(sum.amount()).isEqualByComparingTo("10.30");
    }

    @Test
    void rejectsNullAmount() {
        assertThatThrownBy(() -> new Money(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("amount");
    }

    @Test
    void isPositiveOnlyWhenGreaterThanZero() {
        assertThat(Money.of("0.01").isPositive()).isTrue();
        assertThat(Money.of("0").isPositive()).isFalse();
        assertThat(Money.of("-1").isPositive()).isFalse();
    }
}
