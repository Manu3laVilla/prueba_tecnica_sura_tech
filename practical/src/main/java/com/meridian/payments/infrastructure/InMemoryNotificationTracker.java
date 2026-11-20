package com.meridian.payments.infrastructure;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.meridian.payments.core.CanonicalPaymentEvent;
import com.meridian.payments.core.CanonicalStatus;
import com.meridian.payments.core.IntakeDecision;
import com.meridian.payments.core.NotificationTracker;

@Component
public class InMemoryNotificationTracker implements NotificationTracker {

    private final Map<String, TrackingRecord> byPaymentKey = new ConcurrentHashMap<>();

    @Override
    public IntakeDecision evaluate(CanonicalPaymentEvent event) {
        var payment = event.payment();
        TrackingRecord existing = byPaymentKey.get(payment.paymentKey());

        if (existing == null) {
            return IntakeDecision.ACCEPT_AND_PUBLISH;
        }

        if (payment.providerEventId().equals(existing.lastProviderEventId())) {
            return IntakeDecision.ACCEPT_DUPLICATE_SKIP;
        }

        if (payment.status() == existing.lastStatus()
                && !payment.statusChangedAt().isAfter(existing.statusChangedAt())) {
            return IntakeDecision.ACCEPT_DUPLICATE_SKIP;
        }

        if (payment.statusChangedAt().isBefore(existing.statusChangedAt())) {
            return IntakeDecision.ACCEPT_DUPLICATE_SKIP;
        }

        if (!isAllowedTransition(existing.lastStatus(), payment.status())) {
            return IntakeDecision.ACCEPT_DUPLICATE_SKIP;
        }

        return IntakeDecision.ACCEPT_AND_PUBLISH;
    }

    @Override
    public void recordReceived(CanonicalPaymentEvent event, IntakeDecision decision) {
        if (decision == IntakeDecision.ACCEPT_DUPLICATE_SKIP) {
            return;
        }
        var payment = event.payment();
        byPaymentKey.put(
                payment.paymentKey(),
                new TrackingRecord(
                        payment.providerEventId(),
                        payment.status(),
                        payment.statusChangedAt(),
                        event.metadata().correlationId(),
                        Instant.now()));
    }

    private boolean isAllowedTransition(CanonicalStatus current, CanonicalStatus incoming) {
        if (current == incoming) {
            return true;
        }
        return switch (current) {
            case Pending -> true;
            case Captured -> incoming == CanonicalStatus.Refunded;
            case Failed, Refunded -> false;
        };
    }

    private record TrackingRecord(
            String lastProviderEventId,
            CanonicalStatus lastStatus,
            Instant statusChangedAt,
            String correlationId,
            Instant recordedAt) {
    }
}