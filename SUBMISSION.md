# Submission

## Candidate

| Field | Value |
|---|---|
| Candidate ID (provided by your recruiter) | *(add your email before send)* |
| Final commit SHA | *(run `git rev-parse HEAD` before send)* |
| Date and time of submission (with time zone) | |

## Time spent

| Part | Approximate time |
|---|---|
| A. Integration and data design | ~2 h |
| B. Integration component | ~2 h |
| C. Azure IaC and CI/CD | ~1 h |
| D. Operations | ~0.75 h |
| E. Code review | ~0.75 h |
| F. Responsible AI | ~0.5 h |
| **Total** | ~7 h |

## Stack

- Practical part (Part B): Java 21 + Spring Boot 3.3.6
- Infrastructure as code: Terraform (azurerm ~> 3.117)
- Pipeline: Azure Pipelines (`pipelines/azure-pipelines-*.yml`)
- CI run link (optional):

## Where to start reading

1. `docs/design/SOLUTION_DESIGN.md` — end-to-end flow and capacity.
2. `docs/adr/0001-integration-with-salesforce.md` — Salesforce async/batch strategy.
3. `practical/README.md` — intake implementation and tests.

## Known gaps and what I would do next

- Implement Austral adapter and Cosmos/Service Bus SDK bindings behind existing interfaces.
- Wire APIM policy pack and private endpoints for prod Terraform.
- Add Salesforce consumer module with composite batching and non-retryable 400 handling (per INC-0412).
- Run full CI in Azure DevOps once Maven Central TLS allows dependency download.