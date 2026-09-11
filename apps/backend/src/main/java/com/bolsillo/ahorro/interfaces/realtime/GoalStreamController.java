package com.bolsillo.ahorro.interfaces.realtime;

import com.bolsillo.ahorro.infrastructure.realtime.SseHub;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class GoalStreamController {

    private final SseHub hub;

    public GoalStreamController(SseHub hub) {
        this.hub = hub;
    }

    @GetMapping(path = "/api/goals/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return hub.subscribe();
    }
}
