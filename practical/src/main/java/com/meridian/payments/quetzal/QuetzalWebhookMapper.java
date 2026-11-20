package com.meridian.payments.quetzal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridian.payments.core.CanonicalPaymentEvent;
import com.meridian.payments.core.CanonicalStatus;
import com.meridian.payments.core.PolicyNumberNormalizer;

@Component
public class QuetzalWebhookMapper {

    private final ObjectMapper objectMapper;
    private final PolicyNumberNormalizer policyNormalizer;

    public QuetzalWebhookMapper(ObjectMapper objectMapper, PolicyNumberNormalizer policyNormalizer) {
        this.objectMapper = objectMapper;
        this.policyNormalizer = policyNormalizer;
    }

    public CanonicalPaymentEvent.Builder map(byte[] rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String eventId = root.path("id").asText();
            String type = root.path("type").asText();
            JsonNode charge = root.path("data").path("charge");
            String chargeId = charge.path("id").asText();
            String reference = charge.path("reference").asText();
            int amountMinor = charge.path("amount").asInt();
            String currency = charge.path("currency").asText().toUpperCase();

            CanonicalStatus status = mapStatus(type);
            Instant statusChangedAt = resolveTimestamp(charge, type, root.path("created").asLong());

            String amount = new BigDecimal(amountMinor)
                    .movePointLeft(2)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString();

            String method = charge.path("payment_method").path("type").asText("unknown");

            return new CanonicalPaymentEvent.Builder()
                    .country("MX")
                    .provider("Quetzal")
                    .paymentKey("QZ-" + chargeId)
                    .policyNumber(policyNormalizer.normalize(reference, "MX"))
                    .providerTransactionId(chargeId)
                    .providerEventId(eventId)
                    .amount(amount, currency)
                    .status(status)
                    .statusChangedAt(statusChangedAt)
                    .paymentMethod(method);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Quetzal payload", e);
        }
    }

    private CanonicalStatus mapStatus(String type) {
        return switch (type) {
            case "charge.succeeded" -> CanonicalStatus.Captured;
            case "charge.failed" -> CanonicalStatus.Failed;
            case "charge.refunded" -> CanonicalStatus.Refunded;
            default -> CanonicalStatus.Pending;
        };
    }

    private Instant resolveTimestamp(JsonNode charge, String type, long createdEpoch) {
        Long paidAt = charge.path("paid_at").isNumber() ? charge.path("paid_at").asLong() : null;
        Long refundedAt = charge.path("refunded_at").isNumber() ? charge.path("refunded_at").asLong() : null;
        long epoch = switch (type) {
            case "charge.succeeded" -> paidAt != null ? paidAt : createdEpoch;
            case "charge.refunded" -> refundedAt != null ? refundedAt : createdEpoch;
            default -> createdEpoch;
        };
        return Instant.ofEpochSecond(epoch);
    }
}