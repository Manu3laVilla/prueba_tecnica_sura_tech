# AI guidelines — Payments Integration squad

## Allowed use

- Boilerplate (OpenAPI, Terraform modules, test scaffolds) with human review.
- Explaining provider specs and Salesforce constraints.
- Drafting ADRs **outlines** — engineer owns final decision text.

## Never share with AI tools

- Production Key Vault secrets, Salesforce client secrets, webhook live secrets.
- Customer PII, full webhook payloads from prod, contact-center transcripts.
- Unreleased financial or underwriting data.

## Validation before merge

1. **Tests:** provider test vectors and idempotency cases must pass in CI.
2. **Contract check:** canonical JSON Schema validation on golden files.
3. **Security:** secret scanning; no PII in logs; manual review on auth code.
4. **Spot-check:** engineer re-derives one HMAC or batch calculation per PR.

## Disclosure in pull requests

- PR template checkbox: “AI used” + brief list of areas.
- Link to updated squad runbook when AI shaped design decisions.

## When a service calls an LLM (e.g. DLQ summarisation)

Minimum controls:

- **No PII** in prompts — only ids, statuses, error codes; redact in middleware.
- **Human-in-the-loop** for any action beyond read-only summary.
- **Rate limits** and audit log of prompts/responses (90-day retention).
- **Model supply chain:** approved enterprise endpoint only; no shadow APIs.
- **Fallback:** feature off if LLM unavailable — ops uses Cosmos lookup (FR7).