# Infrastructure as code (Terraform)

## Tooling

| Tool | Version |
|---|---|
| Terraform | ≥ 1.5 |
| AzureRM provider | ~> 3.117 |

## Layout

```text
infra/
  main.tf              # composition
  variables.tf
  outputs.tf
  environments/        # tfvars per environment
  modules/
    key_vault/
    cosmos/
    service_bus/
    container_apps/
    monitoring/
```

## Parameters

- `environment`: `dev`, `test`, `prod`
- `countries`: default `["mx","co","cl"]` — one Service Bus queue per country with DLQ
- `location`: single Azure region (`eastus2` default)

## Validation (no deploy)

```bash
cd infra
terraform init -backend=false
terraform validate
terraform plan -var-file=environments/dev.example.tfvars
```

Run the same with `test.example.tfvars` and `prod.example.tfvars` to review environment differences (replica counts, purge protection on Key Vault).

**No `terraform apply` is required** for the assessment. Run `terraform validate` locally before submission when provider download succeeds.

## Secrets and identity

- Webhook secrets and Salesforce client credentials are stored in **Key Vault**.
- **User-assigned managed identities** on intake and consumer Container Apps.
- Role assignments in `main.tf`: Service Bus **Data Sender** (intake) and **Data Receiver** (consumer). Cosmos SQL RBAC for the intake identity is documented in the design and applied in deployment via the same pattern (assignment commented in root module until subscription id is known).

## CI/CD

See [`pipelines/README.md`](../pipelines/README.md) for quality and security gate justification.

## Known gaps

- Remote state backend (azurerm storage) not configured — add per org standard before production.
- Private endpoints for Cosmos/Key Vault in `prod` assumed but not fully wired.
- APIM is managed by the integration platform team — not in this module.