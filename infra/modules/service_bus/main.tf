variable "resource_group_name" { type = string }
variable "location" { type = string }
variable "environment" { type = string }
variable "project_name" { type = string }
variable "countries" { type = list(string) }
variable "tags" { type = map(string) }

resource "azurerm_servicebus_namespace" "this" {
  name                = "sb-${var.project_name}-${var.environment}"
  location            = var.location
  resource_group_name = var.resource_group_name
  sku                 = var.environment == "prod" ? "Standard" : "Standard"
  tags                = var.tags
}

resource "azurerm_servicebus_queue" "country" {
  for_each            = toset(var.countries)
  name                = "payments-${each.value}"
  namespace_id        = azurerm_servicebus_namespace.this.id
  max_delivery_count  = 10
  lock_duration       = "PT30S"
  dead_lettering_on_message_expiration = true
  requires_duplicate_detection          = true
  duplicate_detection_history_time_window = "PT10M"
}

resource "azurerm_servicebus_topic" "canonical" {
  name         = "premium-payment-events"
  namespace_id = azurerm_servicebus_namespace.this.id
}

resource "azurerm_servicebus_subscription" "finance" {
  name               = "finance"
  topic_id           = azurerm_servicebus_topic.canonical.id
  max_delivery_count = 10
}

resource "azurerm_servicebus_subscription" "notifications" {
  name               = "notifications"
  topic_id           = azurerm_servicebus_topic.canonical.id
  max_delivery_count = 10
}

output "namespace_id" { value = azurerm_servicebus_namespace.this.id }
output "namespace_name" { value = azurerm_servicebus_namespace.this.name }
output "queue_names" {
  value = [for q in azurerm_servicebus_queue.country : q.name]
}