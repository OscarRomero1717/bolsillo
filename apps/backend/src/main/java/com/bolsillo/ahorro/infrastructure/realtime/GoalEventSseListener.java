package com.bolsillo.ahorro.infrastructure.realtime;

import com.bolsillo.ahorro.application.GetGoalUseCase;
import com.bolsillo.ahorro.domain.event.GoalCompletedEvent;
import com.bolsillo.ahorro.domain.event.GoalUpdatedEvent;
import com.bolsillo.ahorro.domain.model.GoalId;
import com.bolsillo.ahorro.interfaces.rest.GoalResponse;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer: domain events → SSE fan-out. Does not belong to the use case.
 */
@Component
public class GoalEventSseListener {

    static final String GOAL_UPDATED = "goal-updated";
    static final String GOAL_COMPLETED = "goal-completed";

    private final SseHub hub;
    private final GetGoalUseCase getGoal;

    public GoalEventSseListener(SseHub hub, GetGoalUseCase getGoal) {
        this.hub = hub;
        this.getGoal = getGoal;
    }

    @EventListener
    public void onGoalUpdated(GoalUpdatedEvent event) {
        push(GOAL_UPDATED, event.goalId());
    }

    @EventListener
    public void onGoalCompleted(GoalCompletedEvent event) {
        push(GOAL_COMPLETED, event.goalId());
    }

    private void push(String eventName, GoalId id) {
        try {
            GoalResponse payload = GoalResponse.from(getGoal.execute(id));
            hub.broadcast(eventName, payload);
        } catch (RuntimeException ignored) {
            // Push must not fail the HTTP command; the contribution is already saved.
        }
    }
}
