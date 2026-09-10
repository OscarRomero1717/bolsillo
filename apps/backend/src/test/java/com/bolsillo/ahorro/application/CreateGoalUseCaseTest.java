package com.bolsillo.ahorro.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.bolsillo.ahorro.application.fake.InMemoryGoalRepository;
import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.GoalStatus;
import com.bolsillo.ahorro.domain.model.Money;
import org.junit.jupiter.api.Test;

class CreateGoalUseCaseTest {

    @Test
    void createPersistsGoalInTheFakeRepository() {
        InMemoryGoalRepository repository = new InMemoryGoalRepository();
        CreateGoalUseCase useCase = new CreateGoalUseCase(repository);

        Goal created = useCase.execute("Viaje", Money.of("1000000"));

        assertThat(created.name().value()).isEqualTo("Viaje");
        assertThat(created.status()).isEqualTo(GoalStatus.OPEN);
        assertThat(repository.findById(created.id())).contains(created);
    }
}
