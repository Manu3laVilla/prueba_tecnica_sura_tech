provider "azurerm" {
  features {
    key_vault {
      purge_soft_deleted_secrets_on_destroy = false
    }
  }
}

locals {
  tags = merge(var.tags, {
    Environment = var.environment
    Service     = "premium-payment-notifications"
  })
}

resource "azurerm_resource_group" "payments" {
  name     = "rg-${var.project_name}-${var.environment}"
  location = var.location
  tags     = local.tags
}

module "key_vault" {
  source              = "./modules/key_vault"
  resource_group_name = azurerm_resource_group.payments.name
  location            = var.location
  environment         = var.environment
  project_name        = var.project_name
  tags                = local.tags
}

module "cosmos" {
  source              = "./modules/cosmos"
  resource_group_name = azurerm_resource_group.payments.name
  location            = var.location
  environment         = var.environment
  project_name        = var.project_name
  tags                = local.tags
}

module "service_bus" {
  source              = "./modules/service_bus"
  resource_group_name = azurerm_resource_group.payments.name
  location            = var.location
  environment         = var.environment
  project_name        = var.project_name
  countries           = var.countries
  tags                = local.tags
}

module "container_apps" {
  source              = "./modules/container_apps"
  resource_group_name = azurerm_resource_group.payments.name
  location            = var.location
  environment         = var.environment
  project_name        = var.project_name
  tags                = local.tags

  key_vault_id        = module.key_vault.id
  cosmos_endpoint     = module.cosmos.endpoint
  servicebus_namespace = module.service_bus.namespace_id
}

module "monitoring" {
  source              = "./modules/monitoring"
  resource_group_name = azurerm_resource_group.payments.name
  location            = var.location
  environment         = var.environment
  project_name        = var.project_name
  tags                = local.tags
}

resource "azurerm_role_assignment" "intake_servicebus_sender" {
  scope                = module.service_bus.namespace_id
  role_definition_name = "Azure Service Bus Data Sender"
  principal_id         = module.container_apps.intake_identity_principal_id
}

resource "azurerm_role_assignment" "consumer_servicebus_receiver" {
  scope                = module.service_bus.namespace_id
  role_definition_name = "Azure Service Bus Data Receiver"
  principal_id         = module.container_apps.consumer_identity_principal_id
}

# Cosmos SQL RBAC role assignment for intake MI — enable after cosmos module outputs are wired in target subscription.