package com.meridian.payments.quetzal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridian.payments.SupportFiles;
import com.meridian.payments.core.CanonicalPaymentEvent;
import com.meridian.payments.core.CanonicalStatus;
import com.meridian.payments.core.PolicyNumberNormalizer;

class QuetzalWebhookMapperTest {

    private final QuetzalWebhookMapper mapper =
            new QuetzalWebhookMapper(new ObjectMapper(), new PolicyNumberNormalizer());

    @Test
    void mapsSucceededAmountFromCentavos() throws IOException {
        byte[] body = SupportFiles.referenceSample("quetzal-mx", "samples", "01-charge-succeeded.json");
        CanonicalPaymentEvent.Builder builder = mapper.map(body);
        CanonicalPaymentEvent event = builder
                .schemaVersion("1.0.0")
                .eventId("e")
                .correlationId("c")
                .acceptedAt(java.time.Instant.parse("2026-11-28T00:00:00Z"))
                .build();

        assertThat(event.payment().amount().value()).isEqualTo("1250.00");
        assertThat(event.payment().amount().currency()).isEqualTo("MXN");
        assertThat(event.payment().status()).isEqualTo(CanonicalStatus.Captured);
    }
}