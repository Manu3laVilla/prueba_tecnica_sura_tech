package com.meridian.payments.core;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class PolicyNumberNormalizer {

    private static final Pattern FULL = Pattern.compile("^POL-(MX|CO|CL)-\\d{9}$", Pattern.CASE_INSENSITIVE);

    public String normalize(String raw, String countryCode) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String trimmed = raw.trim().toUpperCase(Locale.ROOT);
        if (FULL.matcher(trimmed).matches()) {
            return trimmed;
        }
        String digits = trimmed.replaceAll("\\D", "");
        if (digits.length() == 9) {
            return "POL-" + countryCode + "-" + digits;
        }
        return trimmed;
    }
}