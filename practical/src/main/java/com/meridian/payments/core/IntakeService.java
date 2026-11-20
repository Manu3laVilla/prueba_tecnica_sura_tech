package com.meridian.payments.core;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class IntakeService {

    private static final String SCHEMA_VERSION = "1.0.0";

    private final NotificationTracker tracker;
    private final CanonicalEventPublisher publisher;

    public IntakeService(NotificationTracker tracker, CanonicalEventPublisher publisher) {
        this.tracker = tracker;
        this.publisher = publisher;
    }

    public IntakeDecision accept(CanonicalPaymentEvent.Builder builder) {
        CanonicalPaymentEvent event = builder
                .schemaVersion(SCHEMA_VERSION)
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId())
                .acceptedAt(Instant.now())
                .build();

        IntakeDecision decision = tracker.evaluate(event);
        if (decision == IntakeDecision.ACCEPT_AND_PUBLISH) {
            publisher.publish(event);
        }
        tracker.recordReceived(event, decision);
        return decision;
    }

    private String correlationId() {
        return UUID.randomUUID().toString();
    }
}