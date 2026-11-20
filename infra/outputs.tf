output "resource_group_name" {
  value = azurerm_resource_group.payments.name
}

output "service_bus_namespace" {
  value = module.service_bus.namespace_name
}

output "payment_queue_names" {
  value = module.service_bus.queue_names
}

output "cosmos_account_name" {
  value = module.cosmos.account_name
}