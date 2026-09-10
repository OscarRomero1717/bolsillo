package com.bolsillo.ahorro.application.fake;

import static org.assertj.core.api.Assertions.assertThat;

import com.bolsillo.ahorro.domain.event.GoalUpdatedEvent;
import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.Money;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FakePortsTest {

    @Test
    void saveThenFindByIdReturnsTheSameGoal() {
        InMemoryGoalRepository repository = new InMemoryGoalRepository();
        Goal created = Goal.create("Viaje", Money.of("1000000"));

        repository.save(created);
        Optional<Goal> found = repository.findById(created.id());

        assertThat(found).isPresent();
        assertThat(found.get().name().value()).isEqualTo("Viaje");
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void publishKeepsEventsForLaterAssertions() {
        RecordingGoalEventPublisher publisher = new RecordingGoalEventPublisher();
        Goal goal = Goal.create("Viaje", Money.of("100"));
        GoalUpdatedEvent event = new GoalUpdatedEvent(
                goal.id(), goal.currentAmount(), goal.targetAmount(), goal.status());

        publisher.publish(event);

        assertThat(publisher.published()).containsExactly(event);
    }
}
