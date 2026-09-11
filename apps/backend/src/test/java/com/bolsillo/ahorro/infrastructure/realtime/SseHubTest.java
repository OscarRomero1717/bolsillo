package com.bolsillo.ahorro.infrastructure.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseHubTest {

    @Test
    void subscribeReturnsEmitterWithLongTimeout() {
        SseHub hub = new SseHub();

        SseEmitter emitter = hub.subscribe();

        assertThat(emitter.getTimeout()).isEqualTo(SseHub.TIMEOUT_MS);
        assertThat(hub.subscriberCount()).isEqualTo(1);
    }

    @Test
    void broadcastKeepsEmittersThatAcceptTheSend() {
        SseHub hub = new SseHub();
        hub.track(new SilentEmitter());

        assertThatCode(() -> hub.broadcast("goal-updated", "{\"id\":\"g1\"}"))
                .doesNotThrowAnyException();
        assertThat(hub.subscriberCount()).isEqualTo(1);
    }

    @Test
    void broadcastRemovesEmittersThatFailToSend() {
        SseHub hub = new SseHub();
        hub.track(new SilentEmitter());
        hub.track(new FailingEmitter());

        hub.broadcast("goal-completed", "{\"status\":\"COMPLETED\"}");

        assertThat(hub.subscriberCount()).isEqualTo(1);
    }

    @Test
    void broadcastRequiresEventName() {
        SseHub hub = new SseHub();

        assertThatThrownBy(() -> hub.broadcast(null, "x"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("eventName is required");
    }

    private static final class SilentEmitter extends SseEmitter {
        SilentEmitter() {
            super(SseHub.TIMEOUT_MS);
        }

        @Override
        public synchronized void send(SseEventBuilder builder) {
            // Pretend the HTTP connection is alive.
        }
    }

    private static final class FailingEmitter extends SseEmitter {
        FailingEmitter() {
            super(SseHub.TIMEOUT_MS);
        }

        @Override
        public synchronized void send(SseEventBuilder builder) throws IOException {
            throw new IOException("broken pipe");
        }
    }
}
