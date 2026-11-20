# ADR-0001: Integration with Salesforce

- **Status:** Accepted
- **Date:** 2026-11-20

## Context

Meridian must record premium payments in a single Salesforce org (`Premium_Payment__c`) with freshness targets (95% within 5 minutes, 99.5% within 1 hour — NFR2) while respecting ~97% availability, a dedicated API budget of 150,000 REST calls per rolling 24 hours, and a concurrency cap of 25 simultaneous requests (NFR5). Providers require webhook responses within 5–10 seconds (NFR4); Salesforce single-record upsert p95 latency can exceed 1.2s and spike to 8s under load.

Functional requirements FR3–FR5 demand durable upserts by `Payment_Key__c`, correct lifecycle states, and protection against duplicates and out-of-order notifications.

## Options considered

### Option 1 — Synchronous Salesforce write in the webhook request thread

- Pros: Simplest flow; payment visible as soon as the provider receives 2xx.
- Cons: Violates NFR4 under SF latency; couples provider acceptance to Salesforce availability; burns API budget with one call per notification (risk at 120k/day peak).

### Option 2 — Asynchronous consumer with record-by-record upsert

- Pros: Fast webhook response; retries and DLQ isolate Salesforce failures.
- Cons: Up to 120k API calls/day at peak; little headroom for retries and org-wide sharing.

### Option 3 — Asynchronous consumer with composite batch upsert (≤200 records per call)

- Pros: Reduces API calls by up to two orders of magnitude at peak; aligns with Salesforce composite API; supports `allOrNone: false` per-record outcomes.
- Cons: Slightly higher end-to-end latency for batching window (seconds); more complex error handling per record in a batch response.

## Decision

Adopt **Option 3**: country-scoped Service Bus queues, dedicated **Container Apps** consumer workers, **OAuth client-credentials** with cached tokens, and **composite batch upserts** (target 100–200 records, flush every 2–5 seconds when load is high, sooner when idle).

**400-class data errors** (e.g. unknown policy external id) are classified as **non-retryable**: update tracking store to `failed`, do not infinite-retry; alert operations. **5xx, 401, 403 limit, UNABLE_TO_LOCK_ROW** use exponential backoff with jitter; after max deliveries, messages move to the **country DLQ**.

## Consequences

- Positive: Webhook path stays within provider timeouts; API budget sustainable at month-end peak; failures contained per country.
- Negative: Batch window adds seconds to freshness SLO — mitigated by smaller batches when queue depth is low.
- Risks: Hitting org-wide `REQUEST_LIMIT_EXCEEDED` pauses all integrations — mitigated by budget monitoring, consumer throttling, and alerting at 70%/85% of dedicated budget.