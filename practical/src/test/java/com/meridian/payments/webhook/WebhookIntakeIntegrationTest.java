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
import com.meridian.payments.core.CanonicalStatus;
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
    void quetzalChargeFailedMapsToFailedStatus() throws Exception {
        byte[] body = SupportFiles.referenceSample("quetzal-mx", "samples", "02-charge-failed.json");
        String header = "t=1795889403,v1=d793d3da0a8d4d66e984ada3daa8b9f473a0eef153d99cdd83bb70f60f13ccc9";

        mockMvc.perform(post("/webhooks/quetzal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Quetzal-Signature", header)
                        .content(body))
                .andExpect(status().isOk());

        assertThat(publisher.getPublished()).hasSize(1);
        assertThat(publisher.getPublished().get(0).payment().status()).isEqualTo(CanonicalStatus.Failed);
        assertThat(publisher.getPublished().get(0).payment().paymentKey()).isEqualTo("QZ-ch_8Jm3Np0RqA");
    }

    @Test
    void quetzalChargeRefundedMapsToRefundedStatus() throws Exception {
        byte[] body = SupportFiles.referenceSample("quetzal-mx", "samples", "03-charge-refunded.json");
        String header = "t=1795975203,v1=a61f9d88d861cd4cc63359b5ca6e6d11d55369db7079387dee9a5024ccb2cae6";

        mockMvc.perform(post("/webhooks/quetzal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Quetzal-Signature", header)
                        .content(body))
                .andExpect(status().isOk());

        assertThat(publisher.getPublished()).hasSize(1);
        assertThat(publisher.getPublished().get(0).payment().status()).isEqualTo(CanonicalStatus.Refunded);
    }

    @Test
    void andinaCapturedThenReversaPublishesBothStates() throws Exception {
        byte[] approved = SupportFiles.referenceSample("andina-co", "samples", "02-transaccion-aprobada.json");
        byte[] reversa = SupportFiles.referenceSample("andina-co", "samples", "04-transaccion-reversada.json");

        postAndina(approved, "2026-11-28T14:03:11-05:00", "MFQjfviySi6CjqbWtQcy5spKtXxHIxAvmg5W3PTzPJk=");
        postAndina(reversa, "2026-12-03T09:15:42-05:00", "AfGO+MEffk0AWIEEE3cgyff4hWewoOaDhygCWxs/fNI=");

        assertThat(publisher.getPublished()).hasSize(2);
        assertThat(publisher.getPublished().get(0).payment().status()).isEqualTo(CanonicalStatus.Captured);
        assertThat(publisher.getPublished().get(1).payment().status()).isEqualTo(CanonicalStatus.Refunded);
        assertThat(publisher.getPublished().get(0).payment().paymentKey())
                .isEqualTo(publisher.getPublished().get(1).payment().paymentKey());
    }

    @Test
    void andinaPendienteMapsToPending() throws Exception {
        byte[] body = SupportFiles.referenceSample("andina-co", "samples", "01-transaccion-pendiente.json");
        postAndina(body, "2026-11-28T14:02:59-05:00", "JhaBZfk7eQsoMbbQbThmAx/bCyehBOzKYw8XTP2a40k=");

        assertThat(publisher.getPublished()).hasSize(1);
        assertThat(publisher.getPublished().get(0).payment().status()).isEqualTo(CanonicalStatus.Pending);
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