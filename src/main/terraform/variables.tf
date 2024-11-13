# ------------------------------------------------------------------------------
# Generic variables definition.
# ------------------------------------------------------------------------------
variable "prefix" {
  type = string
  validation {
    condition = (
      length(var.prefix) <= 6
    )
    error_message = "Max length is 6 chars."
  }
}

variable "env" {
  type = string
  validation {
    condition = (
      length(var.env) <= 4
    )
    error_message = "Max length is 4 chars."
  }
}

variable "env_short" {
  type = string
  validation {
    condition = (
      length(var.env_short) <= 1
    )
    error_message = "Max length is 1 chars."
  }
}

variable "location" {
  type    = string
  default = "westeurope"
}

variable "location_short" {
  type        = string
  description = "Location short like eg: neu, weu."
}

variable "tags" {
  type = map(any)
}

variable "domain" {
  type    = string
  default = ""
}

# ------------------------------------------------------------------------------
# Container Apps Environment.
# ------------------------------------------------------------------------------
variable "cae_name" {
  type = string
}

variable "cae_resource_group_name" {
  type = string
}

# ------------------------------------------------------------------------------
# Identity for this Container App.
# ------------------------------------------------------------------------------
variable "id_name" {
  type = string
}

variable "id_resource_group_name" {
  type = string
}

# ------------------------------------------------------------------------------
# General purpose key vault used to protect secrets.
# ------------------------------------------------------------------------------
variable "general_kv_name" {
  type = string
}

variable "general_kv_resource_group_name" {
  type = string
}

# ------------------------------------------------------------------------------
# Key vault for cryptographics operations.
# ------------------------------------------------------------------------------
variable "auth_kv_name" {
  type = string
}

variable "auth_kv_resource_group_name" {
  type = string
}

# ------------------------------------------------------------------------------
# Storage account containing configuration files.
# ------------------------------------------------------------------------------
variable "auth_st_name" {
  type = string
}

variable "auth_st_resource_group_name" {
  type = string
}

# ------------------------------------------------------------------------------
# Names of key vault secrets.
# ------------------------------------------------------------------------------
variable "cosmosdb_account_primary_mongodb_connection_string_kv_secret" {
  type = string
}

variable "cosmosdb_account_secondary_mongodb_connection_string_kv_secret" {
  type = string
}

variable "storage_account_primary_blob_endpoint_kv_secret" {
  type = string
}

variable "key_vault_auth_vault_uri_kv_secret" {
  type = string
}

variable "application_insigths_connection_string_kv_secret" {
  type = string
}

# ------------------------------------------------------------------------------
# Specific to auth microservice.
# ------------------------------------------------------------------------------
variable "mil_auth_quarkus_log_level" {
  type    = string
  default = "ERROR"
}

variable "mil_auth_app_log_level" {
  type    = string
  default = "DEBUG"
}

variable "mil_auth_json_log" {
  type    = bool
  default = true
}

variable "mil_auth_quarkus_rest_client_logging_scope" {
  description = "Scope for Quarkus REST client logging. Allowed values are: all, request-response, none."
  type        = string
  default     = "all"
}

variable "mil_auth_cryptoperiod" {
  type    = number
  default = 86400000
}

variable "mil_auth_keysize" {
  type    = number
  default = 4096
}

variable "mil_auth_access_duration" {
  type    = number
  default = 900
}

variable "mil_auth_refresh_duration" {
  type    = number
  default = 3600
}

variable "mil_auth_image" {
  type = string
}

variable "mil_auth_cpu" {
  type    = number
  default = 1
}

variable "mil_auth_memory" {
  type    = string
  default = "2Gi"
}

variable "mil_auth_max_replicas" {
  type    = number
  default = 10
}

variable "mil_auth_min_replicas" {
  type    = number
  default = 1
}

variable "mil_auth_keyvault_maxresults" {
  type    = number
  default = 20
}

variable "mil_auth_keyvault_backoff_num_of_attempts" {
  type    = number
  default = 3
}

variable "mil_auth_mongodb_connect_timeout" {
  type    = string
  default = "5s"
}

variable "mil_auth_mongodb_read_timeout" {
  type    = string
  default = "10s"
}

variable "mil_auth_mongodb_server_selection_timeout" {
  type    = string
  default = "5s"
}

variable "mil_auth_base_url" {
  type = string
}