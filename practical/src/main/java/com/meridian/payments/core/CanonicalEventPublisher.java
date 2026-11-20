package com.meridian.payments.core;

public interface CanonicalEventPublisher {

    void publish(CanonicalPaymentEvent event);

    String countryQueueName(String countryCode);
}