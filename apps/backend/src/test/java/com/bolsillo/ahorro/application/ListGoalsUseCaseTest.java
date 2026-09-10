package com.bolsillo.ahorro.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.bolsillo.ahorro.application.fake.InMemoryGoalRepository;
import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.Money;
import org.junit.jupiter.api.Test;

class ListGoalsUseCaseTest {

    @Test
    void emptyRepositoryReturnsEmptyList() {
        ListGoalsUseCase useCase = new ListGoalsUseCase(new InMemoryGoalRepository());

        assertThat(useCase.execute()).isEmpty();
    }

    @Test
    void listsTheSinglePersistedGoal() {
        InMemoryGoalRepository repository = new InMemoryGoalRepository();
        Goal created = new CreateGoalUseCase(repository).execute("Viaje", Money.of("100"));
        ListGoalsUseCase useCase = new ListGoalsUseCase(repository);

        assertThat(useCase.execute()).containsExactly(created);
    }
}
