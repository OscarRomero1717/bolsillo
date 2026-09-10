package com.bolsillo.ahorro.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.Money;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class GoalHttpDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void fromMapsGoalAndProgressPercent() {
        Goal goal = Goal.create("Viaje", Money.of("100"));
        goal.contribute(Money.of("40"));

        GoalResponse response = GoalResponse.from(goal);

        assertThat(response.name()).isEqualTo("Viaje");
        assertThat(response.targetAmount()).isEqualByComparingTo("100.00");
        assertThat(response.currentAmount()).isEqualByComparingTo("40.00");
        assertThat(response.progressPercent()).isEqualTo(40);
        assertThat(response.status()).isEqualTo("OPEN");
        assertThat(response.version()).isZero();
    }

    @Test
    void rejectsBlankNameAndNonPositiveTarget() {
        CreateGoalRequest request = new CreateGoalRequest("  ", BigDecimal.ZERO);

        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("name", "targetAmount");
    }

    @Test
    void rejectsNonPositiveContributionAmount() {
        ContributionRequest request = new ContributionRequest(new BigDecimal("-1"));

        assertThat(validator.validate(request)).isNotEmpty();
    }
}
