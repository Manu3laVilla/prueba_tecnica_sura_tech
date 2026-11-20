package com.meridian.payments.core;

public interface NotificationTracker {

    IntakeDecision evaluate(CanonicalPaymentEvent event);

    void recordReceived(CanonicalPaymentEvent event, IntakeDecision decision);
}