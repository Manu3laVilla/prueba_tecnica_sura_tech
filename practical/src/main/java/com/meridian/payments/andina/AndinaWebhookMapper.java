package com.meridian.payments.andina;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridian.payments.core.CanonicalPaymentEvent;
import com.meridian.payments.core.CanonicalStatus;
import com.meridian.payments.core.PolicyNumberNormalizer;

@Component
public class AndinaWebhookMapper {

    private final ObjectMapper objectMapper;
    private final PolicyNumberNormalizer policyNormalizer;

    public AndinaWebhookMapper(ObjectMapper objectMapper, PolicyNumberNormalizer policyNormalizer) {
        this.objectMapper = objectMapper;
        this.policyNormalizer = policyNormalizer;
    }

    public CanonicalPaymentEvent.Builder map(byte[] rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String notificationId = root.path("notificacion_id").asText();
            JsonNode tx = root.path("transaccion");
            String transaccionId = tx.path("id").asText();
            String estado = tx.path("estado").asText();
            String reference = tx.path("referencia").asText();
            String valor = tx.path("valor").asText();
            String moneda = tx.path("moneda").asText();
            String medio = tx.path("medio_pago").asText();
            Instant changedAt = Instant.parse(tx.path("fecha_transaccion").asText());

            return new CanonicalPaymentEvent.Builder()
                    .country("CO")
                    .provider("Andina")
                    .paymentKey("AN-" + transaccionId)
                    .policyNumber(policyNormalizer.normalize(reference, "CO"))
                    .providerTransactionId(transaccionId)
                    .providerEventId(notificationId)
                    .amount(valor, moneda)
                    .status(mapStatus(estado))
                    .statusChangedAt(changedAt)
                    .paymentMethod(medio);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Andina payload", e);
        }
    }

    private CanonicalStatus mapStatus(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> CanonicalStatus.Pending;
            case "APROBADA" -> CanonicalStatus.Captured;
            case "RECHAZADA" -> CanonicalStatus.Failed;
            case "REVERSADA" -> CanonicalStatus.Refunded;
            default -> CanonicalStatus.Pending;
        };
    }
}