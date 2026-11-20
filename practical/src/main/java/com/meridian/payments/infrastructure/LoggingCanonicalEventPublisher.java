package com.meridian.payments.infrastructure;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.meridian.payments.core.CanonicalEventPublisher;
import com.meridian.payments.core.CanonicalPaymentEvent;

/**
 * Stub message broker — stores events in memory for tests and logs a safe summary (no PII).
 */
@Component
public class LoggingCanonicalEventPublisher implements CanonicalEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingCanonicalEventPublisher.class);

    private final List<CanonicalPaymentEvent> published = new ArrayList<>();

    @Override
    public void publish(CanonicalPaymentEvent event) {
        published.add(event);
        log.info(
                "Published canonical event country={} paymentKey={} status={} correlationId={}",
                event.metadata().country(),
                event.payment().paymentKey(),
                event.payment().status(),
                event.metadata().correlationId());
    }

    @Override
    public String countryQueueName(String countryCode) {
        return "payments-" + countryCode.toLowerCase();
    }

    public List<CanonicalPaymentEvent> getPublished() {
        return List.copyOf(published);
    }

    public void clear() {
        published.clear();
    }
}