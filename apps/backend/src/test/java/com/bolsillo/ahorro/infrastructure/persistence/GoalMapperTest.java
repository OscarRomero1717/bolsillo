package com.bolsillo.ahorro.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.GoalStatus;
import com.bolsillo.ahorro.domain.model.Money;
import org.junit.jupiter.api.Test;

class GoalMapperTest {

    private final GoalMapper mapper = new GoalMapper();

    @Test
    void roundTripKeepsAmountsStatusAndVersion() {
        Goal original = Goal.create("Viaje", Money.of("100"));
        original.contribute(Money.of("40"));
        GoalJpaEntity entity = mapper.toEntity(original);
        Goal restored = mapper.toDomain(entity);

        assertThat(restored.id()).isEqualTo(original.id());
        assertThat(restored.name().value()).isEqualTo("Viaje");
        assertThat(restored.currentAmount().amount()).isEqualByComparingTo("40.00");
        assertThat(restored.status()).isEqualTo(GoalStatus.OPEN);
        assertThat(restored.version()).isEqualTo(original.version());
        assertThat(entity.getTargetAmount()).isEqualByComparingTo("100.00");
        assertThat(entity.getCurrentAmount()).isEqualByComparingTo("40.00");
    }
}
