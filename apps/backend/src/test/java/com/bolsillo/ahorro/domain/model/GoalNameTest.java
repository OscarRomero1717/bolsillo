package com.bolsillo.ahorro.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class GoalNameTest {

    @Test
    void trimsValue() {
        assertThat(new GoalName("  Viaje  ").value()).isEqualTo("Viaje");
    }

    @Test
    void rejectsEmpty() {
        assertThatThrownBy(() -> new GoalName(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> new GoalName("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
