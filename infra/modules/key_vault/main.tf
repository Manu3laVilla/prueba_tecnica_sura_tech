variable "resource_group_name" { type = string }
variable "location" { type = string }
variable "environment" { type = string }
variable "project_name" { type = string }
variable "tags" { type = map(string) }

resource "azurerm_key_vault" "this" {
  name                       = "kv-${var.project_name}-${var.environment}"
  location                   = var.location
  resource_group_name        = var.resource_group_name
  tenant_id                  = data.azurerm_client_config.current.tenant_id
  sku_name                   = "standard"
  soft_delete_retention_days = 7
  purge_protection_enabled   = var.environment == "prod"
  enable_rbac_authorization  = true
  tags                       = var.tags
}

data "azurerm_client_config" "current" {}

output "id" {
  value = azurerm_key_vault.this.id
}