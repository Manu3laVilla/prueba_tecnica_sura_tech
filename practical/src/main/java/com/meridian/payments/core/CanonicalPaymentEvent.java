package com.meridian.payments.core;

import java.time.Instant;

public record CanonicalPaymentEvent(
        Metadata metadata,
        Payment payment) {

    public record Metadata(
            String schemaVersion,
            String eventId,
            String correlationId,
            String country,
            String provider,
            Instant acceptedAt) {
    }

    public record Payment(
            String paymentKey,
            String policyNumber,
            String providerTransactionId,
            String providerEventId,
            Money amount,
            CanonicalStatus status,
            Instant statusChangedAt,
            String paymentMethod) {
    }

    public record Money(String value, String currency) {
    }

    public static final class Builder {
        private String schemaVersion;
        private String eventId;
        private String correlationId;
        private String country;
        private String provider;
        private Instant acceptedAt;
        private String paymentKey;
        private String policyNumber;
        private String providerTransactionId;
        private String providerEventId;
        private String amountValue;
        private String amountCurrency;
        private CanonicalStatus status;
        private Instant statusChangedAt;
        private String paymentMethod;

        public Builder schemaVersion(String schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder acceptedAt(Instant acceptedAt) {
            this.acceptedAt = acceptedAt;
            return this;
        }

        public Builder paymentKey(String paymentKey) {
            this.paymentKey = paymentKey;
            return this;
        }

        public Builder policyNumber(String policyNumber) {
            this.policyNumber = policyNumber;
            return this;
        }

        public Builder providerTransactionId(String providerTransactionId) {
            this.providerTransactionId = providerTransactionId;
            return this;
        }

        public Builder providerEventId(String providerEventId) {
            this.providerEventId = providerEventId;
            return this;
        }

        public Builder amount(String value, String currency) {
            this.amountValue = value;
            this.amountCurrency = currency;
            return this;
        }

        public Builder status(CanonicalStatus status) {
            this.status = status;
            return this;
        }

        public Builder statusChangedAt(Instant statusChangedAt) {
            this.statusChangedAt = statusChangedAt;
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public CanonicalPaymentEvent build() {
            return new CanonicalPaymentEvent(
                    new Metadata(schemaVersion, eventId, correlationId, country, provider, acceptedAt),
                    new Payment(
                            paymentKey,
                            policyNumber,
                            providerTransactionId,
                            providerEventId,
                            new Money(amountValue, amountCurrency),
                            status,
                            statusChangedAt,
                            paymentMethod));
        }
    }
}