# Submission

## Candidate

| Field | Value |
|---|---|
| Candidate ID (provided by your recruiter) | manuelavillap.98@gmail.com |
| Final commit SHA | `7ad8be084c227f877400700b2761eefd4a7724d9` |
| Date and time of submission (with time zone) | *(update when you email the recruiter — must match the commit above)* |

## Time spent

| Part | Approximate time |
|---|---|
| A. Integration and data design | ~2 h |
| B. Integration component | ~2 h 30 min |
| C. Azure IaC and CI/CD | ~1 h 15 min |
| D. Operations | ~1 h |
| E. Code review | ~1 h |
| F. Responsible AI | ~55 min |
| **Total** | **~8 h 40 min** |

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