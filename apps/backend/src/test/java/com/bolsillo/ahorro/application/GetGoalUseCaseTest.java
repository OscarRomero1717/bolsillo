package com.bolsillo.ahorro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bolsillo.ahorro.application.fake.InMemoryGoalRepository;
import com.bolsillo.ahorro.domain.exception.GoalNotFoundException;
import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.domain.model.Money;
import org.junit.jupiter.api.Test;

class GetGoalUseCaseTest {

    @Test
    void returnsExistingGoal() {
        InMemoryGoalRepository repository = new InMemoryGoalRepository();
        Goal created = new CreateGoalUseCase(repository).execute("Viaje", Money.of("100"));
        GetGoalUseCase useCase = new GetGoalUseCase(repository);

        assertThat(useCase.execute(created.id())).isEqualTo(created);
    }

    @Test
    void missingIdThrowsGoalNotFound() {
        GetGoalUseCase useCase = new GetGoalUseCase(new InMemoryGoalRepository());
        GoalId unknown = GoalId.newId();

        assertThatThrownBy(() -> useCase.execute(unknown))
                .isInstanceOf(GoalNotFoundException.class)
                .extracting(ex -> ((GoalNotFoundException) ex).code())
                .isEqualTo(GoalNotFoundException.GOAL_NOT_FOUND);
    }
}
