# ADR-0002: Implementation stack and data store

- **Status:** Accepted
- **Date:** 2026-11-20

## Context

The squad needs a production-shaped integration for three countries (NFR9), with a practical deliverable for Mexico and Colombia (Part B). The enterprise ecosystem already includes API Management, Mule 4 runtime capability, and a Java Spring Boot prototype for Chile (PR-214). NFR6 requires secrets in Key Vault and managed identities. FR7 requires operational lookup without raw log search.

## Options considered

### Option 1 — Mule 4 applications on Anypoint Runtime

- Pros: Native fit with API Management and DataWeave; MUnit for tests; common in insurer integration squads.
- Cons: Part B deliverable and CI depend on Mule Enterprise artifacts; Azure-native compute (Container Apps) is a second deployment model alongside Mule.

### Option 2 — Java 21 + Spring Boot 3 on Azure Container Apps, Cosmos DB for tracking

- Pros: Aligns with existing Java prototype skills; strong webhook/HMAC testing story (JUnit 5); single deployable unit for intake and SF consumer; Cosmos fits FR7 document queries and idempotency metadata.
- Cons: More custom framework code than Mule; team must own retry/DLQ patterns explicitly.

### Option 3 — Java on Azure Functions only

- Pros: Consumption pricing; quick HTTP triggers.
- Cons: Splitting intake and batch consumer across function apps increases operational surface; cold start less predictable for 40/s bursts.

## Decision

Choose **Option 2**:

- **Runtime:** Java 21, Spring Boot 3.3+, modular packages per provider (`quetzal`, `andina`, shared `core`).
- **Compute:** Azure Container Apps (intake API and Salesforce consumer as separate apps or profiles).
- **State / FR7 / idempotency:** Azure **Cosmos DB** (serverless, partition key `/country`).
- **Messaging:** Azure Service Bus (queues per country + DLQ, topic for canonical downstream events).
- **Secrets:** Azure Key Vault via managed identity.

MuleSoft remains the mandated **edge** (API Management publication); Spring services sit behind APIM as the implementation chosen for velocity and consistency with the Chile prototype.

## Consequences

- Positive: One language for intake and consumer; Cosmos supports ops queries by provider transaction id; Container Apps autoscale for NFR1 bursts.
- Negative: No out-of-the-box Mule monitoring templates — use Application Insights.
- Risks: Developers might bypass APIM in tests — mitigated by OpenAPI contract and pipeline contract tests.