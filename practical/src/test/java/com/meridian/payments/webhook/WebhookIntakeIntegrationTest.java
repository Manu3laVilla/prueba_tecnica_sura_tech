package com.meridian.payments.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.meridian.payments.SupportFiles;
import com.meridian.payments.infrastructure.LoggingCanonicalEventPublisher;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebhookIntakeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoggingCanonicalEventPublisher publisher;

    @BeforeEach
    void resetPublisher() {
        publisher.clear();
    }

    @Test
    void quetzalChargeSucceededPublishesCanonicalEvent() throws Exception {
        byte[] body = SupportFiles.referenceSample("quetzal-mx", "samples", "01-charge-succeeded.json");
        String header = "t=1795888803,v1=2d07770582c02f15235ac4e027b7732beef86be103c574a1c3b988a4275f5031";

        mockMvc.perform(post("/webhooks/quetzal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Quetzal-Signature", header)
                        .content(body))
                .andExpect(status().isOk());

        assertThat(publisher.getPublished()).hasSize(1);
        assertThat(publisher.getPublished().get(0).payment().paymentKey()).isEqualTo("QZ-ch_7Hk2Lm9QpZ");
    }

    @Test
    void andinaDuplicateRetryIsAcceptedButPublishedOnce() throws Exception {
        byte[] first = SupportFiles.referenceSample("andina-co", "samples", "02-transaccion-aprobada.json");
        byte[] retry = SupportFiles.referenceSample("andina-co", "samples", "03-transaccion-aprobada-reintento.json");

        postAndina(first, "2026-11-28T14:03:11-05:00", "MFQjfviySi6CjqbWtQcy5spKtXxHIxAvmg5W3PTzPJk=");
        postAndina(retry, "2026-11-28T14:08:11-05:00", "CNu6m5X6B9bMKTsi/IzZfGquIh/KdxTyesBtL4ax424=");

        assertThat(publisher.getPublished()).hasSize(1);
    }

  @Test
    void invalidQuetzalSignatureIsRejected() throws Exception {
        byte[] body = SupportFiles.referenceSample("quetzal-mx", "samples", "01-charge-succeeded.json");
        mockMvc.perform(post("/webhooks/quetzal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Quetzal-Signature", "t=1795888803,v1=deadbeef")
                        .content(body))
                .andExpect(status().isUnauthorized());
        assertThat(publisher.getPublished()).isEmpty();
    }

    private void postAndina(byte[] body, String timestamp, String signature) throws Exception {
        mockMvc.perform(post("/webhooks/andina")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Andina-Timestamp", timestamp)
                        .header("X-Andina-Signature", signature)
                        .content(body))
                .andExpect(status().isNoContent());
    }
}