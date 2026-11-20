variable "resource_group_name" { type = string }
variable "location" { type = string }
variable "environment" { type = string }
variable "project_name" { type = string }
variable "tags" { type = map(string) }

resource "azurerm_application_insights" "payments" {
  name                = "appi-${var.project_name}-${var.environment}"
  location            = var.location
  resource_group_name = var.resource_group_name
  application_type    = "other"
  workspace_id        = azurerm_log_analytics_workspace.payments.id
  tags                = var.tags
}

resource "azurerm_log_analytics_workspace" "payments" {
  name                = "log-platform-${var.project_name}-${var.environment}"
  location            = var.location
  resource_group_name = var.resource_group_name
  sku                 = "PerGB2018"
  retention_in_days   = 30
  tags                = var.tags
}

output "app_insights_connection_string" {
  value     = azurerm_application_insights.payments.connection_string
  sensitive = true
}