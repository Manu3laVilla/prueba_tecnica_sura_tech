package com.meridian.payments.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.meridian.payments.infrastructure.InMemoryNotificationTracker;

class InMemoryNotificationTrackerTest {

  @Test
    void skipsRefundedBeforeCapturedOrderingViolation() {
        InMemoryNotificationTracker tracker = new InMemoryNotificationTracker();
        CanonicalPaymentEvent captured = event(CanonicalStatus.Captured, "2026-11-28T14:03:09Z", "ntf-1");
        CanonicalPaymentEvent oldPending = event(CanonicalStatus.Pending, "2026-11-28T14:02:00Z", "ntf-0");

        assertEquals(IntakeDecision.ACCEPT_AND_PUBLISH, tracker.evaluate(captured));
        tracker.recordReceived(captured, IntakeDecision.ACCEPT_AND_PUBLISH);
        assertEquals(IntakeDecision.ACCEPT_DUPLICATE_SKIP, tracker.evaluate(oldPending));
    }

    private CanonicalPaymentEvent event(CanonicalStatus status, String instant, String providerEventId) {
        return new CanonicalPaymentEvent.Builder()
                .schemaVersion("1.0.0")
                .eventId("e1")
                .correlationId("c1")
                .country("CO")
                .provider("Andina")
                .acceptedAt(Instant.parse("2026-11-28T14:03:11Z"))
                .paymentKey("AN-TRX")
                .policyNumber("POL-CO-000187342")
                .providerTransactionId("TRX")
                .providerEventId(providerEventId)
                .amount("185000.00", "COP")
                .status(status)
                .statusChangedAt(Instant.parse(instant))
                .paymentMethod("PSE")
                .build();
    }
}