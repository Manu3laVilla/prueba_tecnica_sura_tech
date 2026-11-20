package com.meridian.payments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "meridian.webhooks")
public record WebhookProperties(
        String quetzalSecret,
        String andinaSecret,
        long quetzalTimestampToleranceSeconds,
        long andinaTimestampToleranceMinutes) {
}