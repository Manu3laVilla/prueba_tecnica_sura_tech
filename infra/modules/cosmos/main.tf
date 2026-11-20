variable "resource_group_name" { type = string }
variable "location" { type = string }
variable "environment" { type = string }
variable "project_name" { type = string }
variable "tags" { type = map(string) }

resource "azurerm_cosmosdb_account" "this" {
  name                = "cosmos-${var.project_name}-${var.environment}"
  location            = var.location
  resource_group_name = var.resource_group_name
  offer_type          = "Standard"
  kind                = "GlobalDocumentDB"

  enable_automatic_failover = false

  capabilities {
    name = "EnableServerless"
  }

  consistency_policy {
    consistency_level = "Session"
  }

  geo_location {
    location          = var.location
    failover_priority = 0
  }

  tags = var.tags
}

resource "azurerm_cosmosdb_sql_database" "tracking" {
  name                = "payments"
  resource_group_name = var.resource_group_name
  account_name        = azurerm_cosmosdb_account.this.name
}

resource "azurerm_cosmosdb_sql_container" "notifications" {
  name                  = "notification-tracking"
  resource_group_name   = var.resource_group_name
  account_name          = azurerm_cosmosdb_account.this.name
  database_name         = azurerm_cosmosdb_sql_database.tracking.name
  partition_key_path      = "/country"
  default_ttl_seconds   = 34560000
}

output "account_name" { value = azurerm_cosmosdb_account.this.name }
output "account_id" { value = azurerm_cosmosdb_account.this.id }
output "endpoint" { value = azurerm_cosmosdb_account.this.endpoint }