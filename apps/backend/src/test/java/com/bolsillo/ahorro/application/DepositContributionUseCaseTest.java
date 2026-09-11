package com.bolsillo.ahorro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bolsillo.ahorro.application.fake.InMemoryGoalRepository;
import com.bolsillo.ahorro.application.fake.RecordingGoalEventPublisher;
import com.bolsillo.ahorro.domain.event.DomainEvent;
import com.bolsillo.ahorro.domain.event.GoalUpdatedEvent;
import com.bolsillo.ahorro.domain.exception.GoalNotFoundException;
import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.domain.model.GoalStatus;
import com.bolsillo.ahorro.domain.model.Money;
import com.bolsillo.ahorro.domain.port.GoalEventPublisher;
import com.bolsillo.ahorro.domain.port.GoalRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DepositContributionUseCaseTest {

    @Test
    void validDepositSavesAndPublishesUpdatedEvent() {
        InMemoryGoalRepository repository = new InMemoryGoalRepository();
        RecordingGoalEventPublisher publisher = new RecordingGoalEventPublisher();
        Goal created = new CreateGoalUseCase(repository).execute("Viaje", Money.of("100"));
        DepositContributionUseCase useCase = new DepositContributionUseCase(repository, publisher);

        Goal updated = useCase.execute(created.id(), Money.of("40"));

        assertThat(updated.currentAmount().amount()).isEqualByComparingTo("40.00");
        assertThat(updated.status()).isEqualTo(GoalStatus.OPEN);
        assertThat(repository.findById(created.id()).orElseThrow().currentAmount().amount())
                .isEqualByComparingTo("40.00");
        assertThat(publisher.published()).hasSize(1);
        assertThat(publisher.published().get(0)).isInstanceOf(GoalUpdatedEvent.class);
    }

    @Test
    void missingGoalDoesNotCallSave() {
        CountingGoalRepository repository = new CountingGoalRepository();
        RecordingGoalEventPublisher publisher = new RecordingGoalEventPublisher();
        DepositContributionUseCase useCase = new DepositContributionUseCase(repository, publisher);
        GoalId unknown = GoalId.newId();

        assertThatThrownBy(() -> useCase.execute(unknown, Money.of("10")))
                .isInstanceOf(GoalNotFoundException.class);
        assertThat(repository.saveCalls()).isZero();
        assertThat(publisher.published()).isEmpty();
    }

    @Test
    void publishesEventsAfterSave() {
        List<String> order = new ArrayList<>();
        GoalRepository repository = new InMemoryGoalRepository() {
            @Override
            public Goal save(Goal goal) {
                order.add("save");
                return super.save(goal);
            }
        };
        GoalEventPublisher publisher = new RecordingGoalEventPublisher() {
            @Override
            public void publish(DomainEvent event) {
                order.add("publish");
                super.publish(event);
            }
        };
        Goal created = new CreateGoalUseCase(repository).execute("Viaje", Money.of("100"));
        order.clear();
        DepositContributionUseCase useCase = new DepositContributionUseCase(repository, publisher);

        useCase.execute(created.id(), Money.of("40"));

        assertThat(order).containsExactly("save", "publish");
    }

    private static final class CountingGoalRepository implements GoalRepository {

        private final InMemoryGoalRepository delegate = new InMemoryGoalRepository();
        private int saveCalls;

        int saveCalls() {
            return saveCalls;
        }

        @Override
        public Goal save(Goal goal) {
            saveCalls++;
            return delegate.save(goal);
        }

        @Override
        public Optional<Goal> findById(GoalId id) {
            return delegate.findById(id);
        }

        @Override
        public List<Goal> findAll() {
            return delegate.findAll();
        }
    }
}
