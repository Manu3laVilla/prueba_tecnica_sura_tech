# Operations — Premium Payment Notifications

## SLIs and SLOs (per country)

| SLI | SLO target | Measurement |
|---|---|---|
| Intake availability | 99.9% / 30d | APIM + ACA HTTP 5xx rate |
| Durability | 100% accepted events enqueued | Cosmos `received` vs provider accept count |
| Freshness | 95% recorded in SF &lt; 5 min (NFR2) | `recordedAt - acceptedAt` from Cosmos |
| Freshness tail | 99.5% &lt; 1 h | same |
| DLQ rate | &lt; 0.1% messages / day | SB metrics per `payments-{cc}` |

Separate error budgets per `MX`, `CO`, `CL`.

## Correlation and instrumentation

- **`correlationId`**: generated at intake, propagated to Service Bus application properties, SF upsert audit, Cosmos record, Application Insights operation_Id link.
- **Log** (structured): `correlationId`, `country`, `providerTransactionId`, `paymentKey`, `processingStatus`, `durationMs`, HTTP/SF status codes.
- **Trace**: intake span → enqueue → consumer → Salesforce child span.
- **Metrics**: `notifications.accepted`, `notifications.duplicate_skipped`, `salesforce.upsert.latency`, `queue.depth`, `dlq.depth` tagged by country.

### Never log

Payer name, national ID, email, full webhook JSON, webhook secrets, Salesforce tokens.

## Alerts

| Alert | Threshold | Route |
|---|---|---|
| DLQ depth (each country) | &gt; 100 for 15 min | PagerDuty Payments on-call |
| SF budget usage | &gt; 85% dedicated 24h budget | Slack + throttle consumer |
| Freshness SLO burn | 2× error budget in 6h | On-call |
| Publish failures | &gt; 1% 5xx on intake 5 min | On-call |
| Cosmos RU throttling | sustained 5 min | Platform + squad |

CO, MX, and CL **each** have DLQ alerts (post INC-0412 lesson).