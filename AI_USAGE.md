# AI usage disclosure

- [ ] I did not use any AI tool in this assessment.

## Tools

| Tool (name and version, if known) | Where you used it (part and files) | What you used it for |
|---|---|---|
| Cursor AI (Composer) | Parts A–F | Drafting design docs, ADRs, Terraform layout, Spring Boot intake, operations docs, PR review, and pipeline YAML from scenario + reference specs |

## Validation

- Cross-checked webhook rules against `reference/providers/*/WEBHOOK_SPEC.md` and reimplemented signature logic with official test vectors in JUnit tests.
- Mapped canonical fields to `reference/salesforce/DATA_MODEL.md` and FR/NFR tables in `SCENARIO.md`.
- Reviewed PR-214 diff manually against NFR6, FR4/FR5, and Salesforce write patterns.
- Intended validation: `mvn test` in `practical/` and `terraform validate` in `infra/` (local Maven hit corporate TLS to Maven Central during submission build).

## What I discarded or corrected

- Initial Quetzal header regex too strict for multiple `v1=` signatures — expanded parser for secret rotation.
- Considered synchronous Salesforce in intake — rejected per ADR-0001 after API budget math.

## Data I shared with AI tools

- Repository scenario documents and fictional provider samples only.
- **No** real secrets, customer data, or employer proprietary material.