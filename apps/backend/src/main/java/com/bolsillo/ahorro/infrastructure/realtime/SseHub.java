package com.bolsillo.ahorro.infrastructure.realtime;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Fan-out of Server-Sent Events to every open browser connection.
 * {@link GoalEventSseListener} calls {@link #broadcast}; {@code GET /api/goals/stream} calls {@link #subscribe()}.
 */
@Component
public class SseHub {

    static final long TIMEOUT_MS = 30L * 60L * 1000L;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        return track(new SseEmitter(TIMEOUT_MS));
    }

    public void broadcast(String eventName, Object payload) {
        Objects.requireNonNull(eventName, "eventName is required");
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException | IllegalStateException ex) {
                emitters.remove(emitter);
                completeQuietly(emitter);
            }
        }
    }

    SseEmitter track(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            completeQuietly(emitter);
        });
        emitter.onError(error -> emitters.remove(emitter));
        return emitter;
    }

    int subscriberCount() {
        return emitters.size();
    }

    private static void completeQuietly(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (IllegalStateException ignored) {
            // already completed or timed out
        }
    }
}
