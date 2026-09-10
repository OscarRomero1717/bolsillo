package com.bolsillo.ahorro.domain.event;

import java.time.Instant;

public interface DomainEvent {

    Instant occurredAt();
}
