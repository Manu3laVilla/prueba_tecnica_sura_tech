package com.meridian.payments.quetzal;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.meridian.payments.config.WebhookProperties;
import com.meridian.payments.core.crypto.HmacSupport;

@Component
public class QuetzalSignatureVerifier {

    private static final Pattern HEADER = Pattern.compile("t=(\\d+)(?:,v1=([a-f0-9]+))+");

    private final WebhookProperties properties;

    public QuetzalSignatureVerifier(WebhookProperties properties) {
        this.properties = properties;
    }

    public boolean isValid(byte[] rawBody, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }
        Matcher matcher = HEADER.matcher(signatureHeader.trim());
        if (!matcher.find()) {
            return false;
        }
        long timestamp = Long.parseLong(matcher.group(1));
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - timestamp) > properties.quetzalTimestampToleranceSeconds()) {
            return false;
        }

        List<String> signatures = extractV1Signatures(signatureHeader);
        byte[] secret = properties.quetzalSecret().getBytes(StandardCharsets.UTF_8);
        String payload = timestamp + "." + new String(rawBody, StandardCharsets.UTF_8);
        byte[] mac = HmacSupport.hmacSha256(secret, payload.getBytes(StandardCharsets.UTF_8));
        String expected = HmacSupport.toHexLower(mac);

        return signatures.stream().anyMatch(sig -> HmacSupport.constantTimeEquals(expected, sig.toLowerCase(Locale.ROOT)));
    }

    private List<String> extractV1Signatures(String header) {
        List<String> sigs = new ArrayList<>();
        int idx = 0;
        while (idx < header.length()) {
            int v1 = header.indexOf("v1=", idx);
            if (v1 < 0) {
                break;
            }
            int start = v1 + 3;
            int end = start;
            while (end < header.length() && header.charAt(end) != ',' && header.charAt(end) != ' ') {
                end++;
            }
            sigs.add(header.substring(start, end));
            idx = end;
        }
        return sigs;
    }
}