package com.bolsillo.ahorro.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GoalIdTest {

    @Test
    void newIdProducesDistinctValues() {
        GoalId first = GoalId.newId();
        GoalId second = GoalId.newId();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void fromRoundTripsToString() {
        GoalId original = GoalId.newId();

        GoalId restored = GoalId.from(original.toString());

        assertThat(restored).isEqualTo(original);
    }
}
