package com.meridian.payments.andina;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.meridian.payments.config.WebhookProperties;
import com.meridian.payments.core.crypto.HmacSupport;

@Component
public class AndinaSignatureVerifier {

    private final WebhookProperties properties;

    public AndinaSignatureVerifier(WebhookProperties properties) {
        this.properties = properties;
    }

    public boolean isValid(byte[] rawBody, String timestampHeader, String signatureHeader) {
        if (timestampHeader == null || signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }
        try {
            Instant timestamp = Instant.parse(timestampHeader);
            long toleranceMinutes = properties.andinaTimestampToleranceMinutes();
            if (Duration.between(timestamp, Instant.now()).abs().toMinutes() > toleranceMinutes) {
                return false;
            }
        } catch (DateTimeParseException e) {
            return false;
        }

        String payload = timestampHeader + "." + new String(rawBody, StandardCharsets.UTF_8);
        byte[] secret = properties.andinaSecret().getBytes(StandardCharsets.UTF_8);
        byte[] mac = HmacSupport.hmacSha256(secret, payload.getBytes(StandardCharsets.UTF_8));
        String expected = Base64.getEncoder().encodeToString(mac);
        return HmacSupport.constantTimeEquals(expected, signatureHeader.trim());
    }
}