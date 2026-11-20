# INC-0412 response

## First 30 minutes

1. **Acknowledge** SEV-2; open war room bridge; confirm scope is **Colombia only** (matches report).
2. **Dashboards:** DLQ depth `payments-co`, main queue depth, Salesforce 400/5xx rates, API budget %.
3. **Sample DLQ messages** (3–5): read `lastError`, `referencia`, `transaccion.id`, `estado`.
4. **Compare** successful SF upsert rate (32,880) vs received (37,215) — ~4.3k gap aligns with DLQ 4,312.
5. **Check recent deploys** (v1.14.0 logging) and Salesforce release (validation rule) — unlikely root for `INVALID_FIELD` policy FK.
6. **Hypothesis:** data errors (policy external id) + **immediate requeue** consuming all 10 deliveries → DLQ.
7. **Mitigate customer pain:** run Salesforce report on suspended policies with matching `transaccion.id` from DLQ samples; manual Case linking for contact center script.
8. **Do not** mass-replay DLQ until root cause confirmed.

## Likely root cause

**Non-retryable Salesforce `400 INVALID_FIELD`** because `referencia` values (`000215560`, `pol-co-000198004`) do not match existing `Policy_Number__c` external ids (`POL-CO-#########`). Consumer treats 400 like transient failure, **abandons** message → 10 fast retries → **MaxDeliveryCountExceeded** → DLQ. Approved payments never recorded → policies stay suspended.

**Evidence:** 43,120 SF 400 responses; DLQ reason uniformly `MaxDeliveryCountExceeded`; error text in snapshot; only CO affected (Andina reference format/checkout issue).

**Confirm:** Query SF for `Policy_Number__c` variants; trace intake logs for normalization (if any); compare Andina checkout pre-fill rules for CO.

## Remediation

1. **Stop the bleeding:** deploy consumer change — classify `INVALID_FIELD` on policy FK as **non-retryable** (complete message, Cosmos `failed`, ops alert). Throttle abandon loop.
2. **Data fix:** correct policy references at source or backfill mapping table; for valid payments, create missing policies via policy team (if legitimate).
3. **Safe DLQ replay:** export DLQ → script validates policy exists → re-publish to `payments-co` with new `correlationId` suffix `.replay` and **dedup** check in Cosmos; process **APROBADA** before **REVERSADA** per `fecha_transaccion` for same `transaccion.id`.
4. **Add DLQ alert** on `payments-co` (parity with MX).

## Prevention

- Policy number **normalization** at intake + metric on reject.
- Integration tests with malformed references → expected `failed`, not DLQ storm.
- Dashboard: SF 400 rate by error code; DLQ depth all countries.
- Runbook link in alert for “INVALID_FIELD policy not found”.