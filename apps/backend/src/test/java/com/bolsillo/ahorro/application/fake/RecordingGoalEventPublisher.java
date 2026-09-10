package com.bolsillo.ahorro.application.fake;

import com.bolsillo.ahorro.domain.event.DomainEvent;
import com.bolsillo.ahorro.domain.port.GoalEventPublisher;
import java.util.ArrayList;
import java.util.List;

public class RecordingGoalEventPublisher implements GoalEventPublisher {

    private final List<DomainEvent> published = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        published.add(event);
    }

    public List<DomainEvent> published() {
        return List.copyOf(published);
    }
}
