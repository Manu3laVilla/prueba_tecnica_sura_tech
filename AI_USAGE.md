# AI usage disclosure

- [ ] I did not use any AI tool in this assessment.

## Tools

| Tool (name and version, if known) | Where you used it (part and files) | What you used it for |
|---|---|---|
| Cursor AI (Composer) | Parts A–F | **Code and document writing assistance only** — turning **my** architecture and requirements into Markdown, Terraform, Java/Spring, tests, and YAML, under my step-by-step instructions |

## My role: decisions, review, and ownership

I used an AI assistant as a **writer/implementer**, not as an architect or decision-maker.

**Before any substantial implementation**, I read `SCENARIO.md`, `TASKS.md`, `SUBMISSION_CHECKLIST.md`, and the full `reference/` folder (providers, Salesforce, INC-0412). I mapped functional and non-functional requirements to deliverables, estimated effort per part, and defined the target solution in my own words (integration flow, idempotency, Salesforce constraints, operations model).

**I made all architecture and development decisions**, including but not limited to:

- Stack: Java 21 + Spring Boot (Part B), Terraform (Part C), Azure Pipelines.
- Async Salesforce consumption with composite batch upserts (ADR-0001) and API budget reasoning.
- Azure Service Bus per country with DLQ, canonical topic for downstream, Cosmos for notification tracking (ADR-0002).
- Durability before provider `2xx` via enqueue + tracking (ADR-0003).
- Canonical payment event shape, `Payment_Key__c` rules, and provider mapping for MX, CO, and CL in design.
- Intake-only scope for Part B (MX/CO), stubs for broker and Cosmos, policy number normalisation lesson from INC-0412.
- CI gates (Maven test + `terraform validate`) and documented CD stages with OIDC.
- Part E verdict and findings on PR-214; Part D runbook and incident response.

The assistant **did not choose** this architecture independently. It produced drafts **after I specified** what the solution should contain.

## How I directed the work

I instructed the assistant to:

- Deliver **part by part** (A → F) with **incremental commits** and English-only artefacts.
- Follow exact paths from `TASKS.md` (`docs/design/`, `practical/`, `infra/`, `pipelines/`, etc.).
- Use Mermaid in design docs, OpenAPI + JSON Schema before code, and official webhook test vectors in tests.
- **Not** push to remote, **not** deploy Azure, **not** call real providers or Salesforce.

When the assistant proposed something that did **not** match my analysis, **I rejected it or sent it back**, for example:

- Implementations that skipped checklist items or left template READMEs in place.
- Shallow test coverage (happy path only) — I required additional cases (failed charge, refund, Andina retry, reversa, pendiente, invalid signature).
- Missing CI/CD **rationale** — I required `pipelines/README.md` and infra/README alignment.
- Gaps between design and infra (Cosmos RBAC wording, example tfvars for test/prod).
- Any approach that did not match my intended integration logic (e.g. synchronous Salesforce on the webhook thread after I had reasoned about NFR4/NFR5).

## Exhaustive review I performed

I personally reviewed (and corrected where needed):

- End-to-end design against FR1–FR7 and NFR1–NFR9.
- ADR trade-offs and consequences.
- Webhook signature rules vs `WEBHOOK_SPEC.md` for Quetzal and Andina.
- Canonical schema and OpenAPI vs intake controllers and mappers.
- Terraform modules for messaging, identities, compute, and observability.
- Operations SLIs/SLOs and INC-0412 root cause / remediation / safe DLQ replay.
- PR-214 review for security, idempotency, Salesforce model, and PII in logs.
- `SUBMISSION.md`, time spent, candidate ID, and submit readiness.

I treated assistant-generated files as **drafts** until they passed my checklist and mental model of the scenario.

## Tests (my quality bar)

The assessment asks for meaningful automated tests beyond the happy path. I directed and verified:

| Area | Location |
|---|---|
| Quetzal HMAC vectors | `practical/src/test/java/.../quetzal/QuetzalSignatureVerifierTest.java` |
| Andina HMAC vectors | `.../andina/AndinaSignatureVerifierTest.java` |
| Mapper / amounts | `.../quetzal/QuetzalWebhookMapperTest.java` |
| FR5 ordering rules | `.../core/InMemoryNotificationTrackerTest.java` |
| End-to-end intake + idempotency | `.../webhook/WebhookIntakeIntegrationTest.java` |
| JSON Schema golden file | `practical/src/test/resources/canonical/sample-captured.json` |

Samples are loaded from `reference/providers/`; tolerances for sample timestamps are in `application-test.yml`. I planned to run `mvn test` before submit (blocked on one network by Maven Central TLS).

## Validation

- Cross-checked behaviour against official provider test vectors and Salesforce `DATA_MODEL.md`.
- Mapped review findings to scenario impact (not generic style comments).
- Confirmed deliverables against `SUBMISSION_CHECKLIST.md`.

## What I discarded or corrected (examples)

- Default or minimal scope when I required full Parts A–F coverage.
- Assistant wording or structure that I rewrote through follow-up instructions to match **my** intended technical narrative.
- Quetzal signature handling until it supported provider rules (e.g. multiple `v1=` signatures).

## Data I shared with AI tools

- Only fictional assessment content in this repository.
- **No** production secrets, real personal data, or employer confidential material.

## Summary

**Decisions, architecture, review depth, and test strategy: mine.** The AI helped **write** the repo faster from my specifications. The submission is work I stand behind in the technical defence.