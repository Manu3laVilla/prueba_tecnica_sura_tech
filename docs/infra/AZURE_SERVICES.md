# Azure services rationale

## Summary

| Service | Role | Tier (assumed) | Alt. discarded |
|---|---|---|---|
| API Management | Webhook edge (platform) | Existing org SKU | Public Container App URL without gateway |
| Container Apps | Intake + SF consumer | Consumption / dedicated env | AKS (ops cost), Functions (cold start) |
| Service Bus Standard | Country queues + DLQ + topic | Standard | Event Hubs (less DLQ ergonomics) |
| Cosmos DB serverless | FR7 + idempotency | Serverless | SQL (higher ops), Table (limited query) |
| Key Vault Standard | Webhook + SF secrets | Standard | App Settings secrets |
| Application Insights | Traces, SLIs | Pay-per-GB | Self-hosted only metrics |
| User-assigned MI | Least-privilege access | — | Connection strings in config |

## Well-Architected notes

- **Reliability:** Per-country queues contain blast radius; DLQ + `max_delivery_count`; batch SF consumer.
- **Security:** MI + RBAC; no secrets in repo; PII-free structured logs.
- **Cost:** Serverless Cosmos for variable ops traffic; SB Standard required for topics and DLQ.
- **Performance:** Container Apps scale to 20 intake replicas for 40 rps bursts (NFR1).

## Order-of-magnitude monthly cost (prod)

Assumptions: 120k peak days 4×/month, otherwise 15k/day; 3 countries; single region.

| Item | Estimate USD/month |
|---|---|
| Container Apps (intake 2–8 replicas avg, consumer 2) | $180–$350 |
| Service Bus Standard (1M ops) | $65 |
| Cosmos serverless (~500k RUs) | $120–$200 |
| Key Vault + App Insights + Log Analytics | $80–$150 |
| **Total** | **~$450–$765** |

Excludes APIM, Salesforce, and egress to providers.