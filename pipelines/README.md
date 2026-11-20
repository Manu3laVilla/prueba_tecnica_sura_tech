# CI/CD rationale

## CI gates (pull requests)

| Gate | Why it is essential |
|---|---|
| **Maven `test` (Java 21)** | Proves webhook signature verification (official test vectors), canonical mapping, and idempotency behaviours for MX/CO — the highest-risk logic in Part B. |
| **`terraform validate`** | Catches wiring errors in messaging, identities, and compute before any human reviews infra diffs; no Azure subscription required. |
| **Dependency / secret scan (placeholder)** | Would block credentials in repo and known CVEs; left as stub echo until org tooling (Mend/OWASP) is wired to Azure DevOps. |

## CD controls (defined, not executed)

| Control | Purpose |
|---|---|
| **Environments** `payments-dev` → `payments-test` → `payments-prod` | Progressive exposure with manual approvals on prod. |
| **OIDC / federated `azureServiceConnection`** | Pipeline authenticates to Azure without storing client secrets in YAML. |
| **Separate infra vs app deploy steps** | Terraform plan/apply (when enabled) before ACA revision rollout to avoid skew between queue and consumer. |

See `azure-pipelines-ci.yml` and `azure-pipelines-cd.yml`.