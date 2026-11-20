# Assumptions

> The scenario is intentionally incomplete. Record every assumption you needed to make, and the part it affects.

| # | Assumption | Why I needed it | Part(s) affected |
|---|---|---|---|
| 1 | API Management validates TLS and applies baseline rate limiting; the intake app trusts `X-Forwarded-*` only from APIM. | APIM routing details are not specified. | A, B, C |
| 2 | Policy numbers missing the `POL-{CC}-` prefix are normalized when the remaining digits match the expected length; otherwise the event is accepted, published, and marked `failed` for Salesforce with reason `INVALID_POLICY_REFERENCE`. | INC-0412 shows raw numeric references; checkout may send shortened refs in CO. | A, B, D |
| 3 | Downstream subscribers use the canonical topic with their own subscriptions; Finance filters `captured`/`refunded`, Notifications filters `captured`. | Consumer behaviour is out of scope. | A |
| 4 | Cosmos DB serverless (NoSQL API) is available in the platform subscription with private endpoints in prod. | Cosmos SKU and networking are not specified. | A, C |
| 5 | Operations tooling authenticates via the corporate IdP (client credentials) to an internal read API backed by Cosmos; no PII is returned. | FR7 UI is not specified. | A, D |
| 6 | Chile (Austral) will follow the same intake library pattern as MX/CO in a follow-up release; Part B implements MX and CO only. | TASKS scope. | B |
| 7 | Service Bus Standard tier with duplicate detection enabled on country queues (optional secondary guard, not a substitute for business idempotency). | Duplicate detection window not specified. | A, C |
| 8 | Month-end peak (120k/day) maps to ~83 msgs/min average with 15-minute bursts at 40/s; Salesforce consumer targets batches of 100–200 records flushed every 2–5 seconds under load. | Exact batch tuning not specified. | A, C |
| 9 | Azure Pipelines agents run `terraform validate` and Maven tests only; no Azure service connection is required for assessment submission. | No Azure subscription for candidates. | C |
| 10 | Test webhook secrets from `reference/providers/*/WEBHOOK_SPEC.md` are used in local/tests profiles only. | NFR6; no real secrets. | B |