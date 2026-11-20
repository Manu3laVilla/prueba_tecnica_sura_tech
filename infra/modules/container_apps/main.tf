variable "resource_group_name" { type = string }
variable "location" { type = string }
variable "environment" { type = string }
variable "project_name" { type = string }
variable "tags" { type = map(string) }
variable "key_vault_id" { type = string }
variable "cosmos_endpoint" { type = string }
variable "servicebus_namespace" { type = string }

resource "azurerm_log_analytics_workspace" "aca" {
  name                = "log-${var.project_name}-${var.environment}"
  location            = var.location
  resource_group_name = var.resource_group_name
  sku                 = "PerGB2018"
  retention_in_days   = 30
  tags                = var.tags
}

resource "azurerm_container_app_environment" "this" {
  name                       = "cae-${var.project_name}-${var.environment}"
  location                   = var.location
  resource_group_name        = var.resource_group_name
  log_analytics_workspace_id = azurerm_log_analytics_workspace.aca.id
  tags                       = var.tags
}

resource "azurerm_user_assigned_identity" "intake" {
  name                = "id-${var.project_name}-intake-${var.environment}"
  location            = var.location
  resource_group_name = var.resource_group_name
  tags                = var.tags
}

resource "azurerm_user_assigned_identity" "consumer" {
  name                = "id-${var.project_name}-consumer-${var.environment}"
  location            = var.location
  resource_group_name = var.resource_group_name
  tags                = var.tags
}

resource "azurerm_container_app" "intake" {
  name                         = "ca-${var.project_name}-intake"
  container_app_environment_id = azurerm_container_app_environment.this.id
  resource_group_name          = var.resource_group_name
  revision_mode                = "Single"
  tags                         = var.tags

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.intake.id]
  }

  template {
    min_replicas = var.environment == "prod" ? 2 : 1
    max_replicas = 20

    container {
      name   = "intake"
      image  = "ghcr.io/meridian/payments-intake:placeholder"
      cpu    = 0.5
      memory = "1Gi"

      env {
        name  = "COSMOS_ENDPOINT"
        value = var.cosmos_endpoint
      }
    }
  }

  ingress {
    external_enabled = true
    target_port      = 8080
    traffic_weight {
      percentage      = 100
      latest_revision = true
    }
  }
}

resource "azurerm_container_app" "consumer" {
  name                         = "ca-${var.project_name}-consumer"
  container_app_environment_id = azurerm_container_app_environment.this.id
  resource_group_name          = var.resource_group_name
  revision_mode                = "Single"
  tags                         = var.tags

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.consumer.id]
  }

  template {
    min_replicas = 1
    max_replicas = 10

    container {
      name   = "consumer"
      image  = "ghcr.io/meridian/payments-consumer:placeholder"
      cpu    = 0.5
      memory = "1Gi"
    }
  }
}

output "intake_identity_principal_id" {
  value = azurerm_user_assigned_identity.intake.principal_id
}

output "consumer_identity_principal_id" {
  value = azurerm_user_assigned_identity.consumer.principal_id
}