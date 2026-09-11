package com.bolsillo.ahorro.infrastructure.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.bolsillo.ahorro.application.CreateGoalUseCase;
import com.bolsillo.ahorro.application.GetGoalUseCase;
import com.bolsillo.ahorro.application.fake.InMemoryGoalRepository;
import com.bolsillo.ahorro.domain.event.GoalCompletedEvent;
import com.bolsillo.ahorro.domain.event.GoalUpdatedEvent;
import com.bolsillo.ahorro.domain.model.Goal;
import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.domain.model.GoalStatus;
import com.bolsillo.ahorro.domain.model.Money;
import com.bolsillo.ahorro.interfaces.rest.GoalResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GoalEventSseListenerTest {

    @Test
    void updatedEventBroadcastsGoalResponse() {
        InMemoryGoalRepository repository = new InMemoryGoalRepository();
        Goal goal = new CreateGoalUseCase(repository).execute("Viaje", Money.of("100"));
        goal.contribute(Money.of("40"));
        repository.save(goal);
        RecordingSseHub hub = new RecordingSseHub();
        GoalEventSseListener listener =
                new GoalEventSseListener(hub, new GetGoalUseCase(repository));

        listener.onGoalUpdated(
                new GoalUpdatedEvent(goal.id(), goal.currentAmount(), goal.targetAmount(), goal.status()));

        assertThat(hub.eventNames).containsExactly(GoalEventSseListener.GOAL_UPDATED);
        GoalResponse payload = (GoalResponse) hub.payloads.get(0);
        assertThat(payload.name()).isEqualTo("Viaje");
        assertThat(payload.progressPercent()).isEqualTo(40);
        assertThat(payload.status()).isEqualTo("OPEN");
    }

    @Test
    void completedEventBroadcastsGoalResponse() {
        InMemoryGoalRepository repository = new InMemoryGoalRepository();
        Goal goal = new CreateGoalUseCase(repository).execute("Viaje", Money.of("100"));
        goal.contribute(Money.of("100"));
        repository.save(goal);
        RecordingSseHub hub = new RecordingSseHub();
        GoalEventSseListener listener =
                new GoalEventSseListener(hub, new GetGoalUseCase(repository));

        listener.onGoalCompleted(new GoalCompletedEvent(goal.id(), goal.name(), goal.targetAmount()));

        assertThat(hub.eventNames).containsExactly(GoalEventSseListener.GOAL_COMPLETED);
        GoalResponse payload = (GoalResponse) hub.payloads.get(0);
        assertThat(payload.status()).isEqualTo("COMPLETED");
        assertThat(payload.progressPercent()).isEqualTo(100);
    }

    @Test
    void missingGoalDoesNotThrow() {
        RecordingSseHub hub = new RecordingSseHub();
        GoalEventSseListener listener =
                new GoalEventSseListener(hub, new GetGoalUseCase(new InMemoryGoalRepository()));

        assertThatCode(() -> listener.onGoalUpdated(
                        new GoalUpdatedEvent(GoalId.newId(), Money.of("10"), Money.of("100"), GoalStatus.OPEN)))
                .doesNotThrowAnyException();
        assertThat(hub.eventNames).isEmpty();
    }

    private static final class RecordingSseHub extends SseHub {
        private final List<String> eventNames = new ArrayList<>();
        private final List<Object> payloads = new ArrayList<>();

        @Override
        public void broadcast(String eventName, Object payload) {
            eventNames.add(eventName);
            payloads.add(payload);
        }
    }
}
