package com.bolsillo.ahorro.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bolsillo.ahorro.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class GoalCreateTest {

    @Test
    void createStartsOpenWithZeroAccumulated() {
        Goal goal = Goal.create("Viaje a Cartagena", Money.of("1000000"));

        assertThat(goal.id()).isNotNull();
        assertThat(goal.name().value()).isEqualTo("Viaje a Cartagena");
        assertThat(goal.targetAmount().amount()).isEqualByComparingTo("1000000.00");
        assertThat(goal.currentAmount().amount()).isEqualByComparingTo("0.00");
        assertThat(goal.status()).isEqualTo(GoalStatus.OPEN);
        assertThat(goal.version()).isZero();
    }

    @Test
    void createRejectsZeroTarget() {
        assertThatThrownBy(() -> Goal.create("Viaje", Money.of("0")))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).code())
                .isEqualTo(Goal.TARGET_NOT_POSITIVE);
    }

    @Test
    void createRejectsNegativeTarget() {
        assertThatThrownBy(() -> Goal.create("Viaje", Money.of("-1")))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).code())
                .isEqualTo(Goal.TARGET_NOT_POSITIVE);
    }
}
