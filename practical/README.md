# Practical part — Payment notification intake (MX, CO)

## Stack

| Component | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.6 |
| Build | Maven 3.9+ |
| Tests | JUnit 5, Spring MockMvc |

## Structure

```text
src/main/java/com/meridian/payments/
  core/           # canonical model, intake orchestration, policy normalizer
  quetzal/        # Mexico provider adapter
  andina/         # Colombia provider adapter
  infrastructure/ # stub broker + in-memory tracker (stand-in for Cosmos + Service Bus)
  config/
```

## Build and test

```bash
cd practical
mvn -q test
```

Integration tests load JSON samples from `../reference/providers/` and official signature vectors from each `WEBHOOK_SPEC.md`.

### Test layout

```text
practical/src/test/java/com/meridian/payments/
  quetzal/QuetzalSignatureVerifierTest.java     # HMAC test vectors (MX)
  quetzal/QuetzalWebhookMapperTest.java         # amount + status mapping
  andina/AndinaSignatureVerifierTest.java       # HMAC test vectors (CO)
  core/InMemoryNotificationTrackerTest.java     # FR5 ordering rules
  webhook/WebhookIntakeIntegrationTest.java     # end-to-end intake + idempotency
practical/src/test/resources/
  application-test.yml                          # widened clock tolerances for sample timestamps
  canonical/sample-captured.json                # JSON Schema golden file
```

Run all tests: `mvn test` from this directory.

## What is stubbed

| Dependency | Production target | Local / test |
|---|---|---|
| Message broker | Azure Service Bus queue + topic | `LoggingCanonicalEventPublisher` |
| Tracking store | Cosmos DB `notification-tracking` | `InMemoryNotificationTracker` |
| Secrets | Azure Key Vault | `application.yml` / env vars (test secrets only) |
| API Management | Edge gateway | Direct controller URLs in tests |

## Known gaps

- Chile (Austral) adapter not implemented — covered in solution design only.
- No live Service Bus or Cosmos SDK calls; switch implementations behind `CanonicalEventPublisher` and `NotificationTracker` interfaces for deployment.
- Salesforce consumer is out of Part B scope (see `docs/design/SOLUTION_DESIGN.md`).
- APIM policies (rate limit, IP allow list) not enforced in local profile.