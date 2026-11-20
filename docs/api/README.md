# API contracts

## Layout

| Artifact | Purpose |
|---|---|
| `webhooks.openapi.yaml` | Inbound webhook surface published via API Management |
| `events/premium-payment.schema.json` | Canonical event on Service Bus topic |

## Versioning

- **Webhook OpenAPI:** bump `info.version` for documentation-only changes; path versioning (`/payments/v2`) when authentication or path structure changes. Providers receive migration notice ≥ 90 days (assumption).
- **Canonical schema:** `metadata.schemaVersion` uses semver. **Minor** bumps may add optional fields. **Major** bumps require a new topic subscription (`premium-payment-events.v2`) so consumers opt in; producers publish to both topics during migration.

## Compatibility rules

1. Never remove or rename required canonical fields without a major version.
2. Provider-specific payloads stay in provider modules; only the canonical form crosses country boundaries.
3. JSON Schema validation runs in CI against golden fixtures under `practical/src/test/resources/canonical/`.

## Validation

```bash
# From repo root (requires Node npx or schema tooling of your choice)
npx --yes ajv-cli validate -s docs/api/events/premium-payment.schema.json -d practical/src/test/resources/canonical/sample-captured.json
```