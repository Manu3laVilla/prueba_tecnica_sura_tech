variable "environment" {
  description = "dev, test, or prod"
  type        = string
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "eastus2"
}

variable "countries" {
  description = "Countries with dedicated payment queues"
  type        = list(string)
  default     = ["mx", "co", "cl"]
}

variable "project_name" {
  type    = string
  default = "meridian-payments"
}

variable "tags" {
  type    = map(string)
  default = {}
}