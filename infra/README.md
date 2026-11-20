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
terraform plan -var-file=environments/dev.tfvars
```

I validated structure with `terraform validate` locally when network allows provider download. **No `terraform apply` is required** for the assessment.

## Secrets and identity

- Webhook secrets and Salesforce client credentials are stored in **Key Vault**.
- **User-assigned managed identities** on intake and consumer Container Apps.
- Role assignments: Service Bus Data Sender/Receiver, Cosmos DB Data Contributor (RBAC).

## Known gaps

- Remote state backend (azurerm storage) not configured — add per org standard before production.
- Private endpoints for Cosmos/Key Vault in `prod` assumed but not fully wired.
- APIM is managed by the integration platform team — not in this module.