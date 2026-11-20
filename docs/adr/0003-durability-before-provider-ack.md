# ADR-0003: Durability before provider acknowledgment

- **Status:** Accepted
- **Date:** 2026-11-20

## Context

NFR3 defines an *accepted* notification as one that received a `2xx` from Meridian; such notifications must never be lost. NFR4 requires fast responses (5s Colombia, 10s Mexico). Salesforce and downstream processing are intentionally asynchronous (ADR-0001).

## Options considered

### Option 1 — Return 2xx immediately, then process in-memory async

- Pros: Fastest response.
- Cons: Process crash loses accepted notifications — violates NFR3.

### Option 2 — Persist to Cosmos then 2xx; publish to Service Bus in a background thread

- Pros: Cosmos durability for acceptance audit.
- Cons: Gap between 2xx and enqueue if the process dies — consumer never sees message.

### Option 3 — Synchronous enqueue to Service Bus (+ Cosmos tracking write) before 2xx

- Pros: Broker durability matches provider acceptance; Cosmos supports FR7 and idempotency.
- Cons: Adds tens of milliseconds; must handle duplicate provider retries via idempotency keys.

## Decision

Adopt **Option 3** with ordering:

1. Verify signature (fail fast with 4xx — not accepted).
2. Map to canonical event; assign/propagate `correlationId`.
3. Apply idempotency / ordering rules against Cosmos.
4. **Send** message to country queue and canonical topic (transactional send where SDK supports, otherwise queue-first with compensating retry).
5. Upsert Cosmos tracking (`received` / `duplicate_skipped`).
6. Return provider-appropriate `2xx`.

If enqueue fails after validation, return **5xx** so the provider retries; idempotency prevents duplicate side effects.

## Consequences

- Positive: Clear alignment between provider retries and platform durability.
- Negative: Service Bus outage blocks acceptance — acceptable; providers retry; alert on sustained publish failures.
- Risks: Double publish on partial failures — mitigated by business idempotency keys and duplicate detection on queues.