# Review — PR-214

## Verdict

- [ ] Approve
- [ ] Approve with comments
- [x] Request changes

## Summary for the author

@dvaldes — thanks for pushing Chile forward ahead of month-end. The async response pattern is the right instinct for Austral’s 10s timeout. Before merge, we need fixes for **secret handling**, **idempotency/ordering across payment states**, **Salesforce write model alignment**, and **PII-safe logging**. Several issues would cause wrong coverage or compliance incidents in production, not just style problems.

## Findings

| # | Severity | File:line | Finding | Why it matters in this scenario | Suggested fix |
|---|---|---|---|---|---|
| 1 | Blocker | `src/main/resources/application.yml:17-18` | Live-format webhook secret committed in config | NFR6; breach of squad policy; Chile path is internet-facing | Load from Key Vault / ACA secretRef; remove from repo; rotate secret |
| 2 | Blocker | `AustralWebhookController.java:193-196` | Signature check skipped when header absent (`if (signature != null && ...)`) | Unauthenticated POST can trigger SF writes → fraudulent Captured payments and wrongful reinstatement | Require header; return 401 when missing/invalid |
| 3 | Major | `AustralPaymentService.java:79-90` | Updates/creates by lookup on provider + transaction id without `Payment_Key__c` upsert or ordering fields | FR4/FR5; duplicate `eventId` skipped but **reversal** is a new `eventId` — update path may race; omits `Status_Changed_At__c` / `Last_Event_Id__c` | Upsert `Premium_Payment__c` by external id `Payment_Key__c`; enforce monotonic `occurredAt` |
| 4 | Major | `AustralPaymentService.java:67-72` | Idempotency keyed only on `eventId` | Reversal events have new ids; out-of-order success after refund can overwrite state | Track `paymentId` + compare provider timestamps; see `DATA_MODEL.md` |
| 5 | Major | `AustralPaymentService.java:92-95` | Swallows all exceptions after accept | NFR3 if HTTP already returned 200 — lost failures with no DLQ/retry visibility | Intake pattern: persist+queue before 2xx; or mark failed + alert; never silent drop |
| 6 | Major | `AustralWebhookController.java:198` | Logs entire `notification` at INFO | NFR6 — payer name/RUT/email in logs | Log ids + correlation only |
| 7 | Major | `AustralPaymentService.java:84-85` | Amount handling uses `double` and unnecessary rounding on CLP | CLP has no minor units — risk of wrong `Amount__c` | Parse integer string to `BigDecimal` without float |
| 8 | Major | `AustralSignatureVerifier.java:138` | `String.equals` for MAC comparison | Timing side-channel on webhook auth | `MessageDigest.isEqual` on hex bytes |
| 9 | Minor | `AustralPaymentServiceTest.java:340` | Duplicate test allows `createPayment` atMost once — does not assert zero calls | Weak guard on FR4 | `verify(salesforceClient, never()).createPayment(...)` |
| 10 | Nit | `AsyncConfig.java:226` | Unbounded fixed thread pool without MDC propagation | Harder ops correlation | Use framework async with context propagation |

## What looks good

- Separate verifier component and HMAC-SHA256 choice matches Austral spec.
- Async processing intent addresses NFR4.
- Tests exist as a starting point (need harder assertions).