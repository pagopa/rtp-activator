# ------------------------------------------------------------------------------
# Container app.
# ------------------------------------------------------------------------------
resource "azurerm_container_app" "rtp-activator" {
  name                         = "${local.project}-activator-ca"
  container_app_environment_id = data.azurerm_container_app_environment.rtp-cae.id
  resource_group_name          = data.azurerm_container_app_environment.rtp-cae.resource_group_name
  revision_mode                = "Single"

  template {
    container {
      name   = "rtp-activator"
      image  = var.rtp_activator_image
      cpu    = var.rtp_activator_cpu
      memory = var.rtp_activator_memory

      liveness_probe {
        port = 8080
        path = "/actuator/health"
        transport = "HTTP"
      }

      readiness_probe {
        port = 8080
        path = "/actuator/health"
        transport = "HTTP"
      }

      startup_probe {
        port = 8080
        path = "/actuator/health"
        transport = "HTTP"
      }

      env {
        name  = "TZ"
        value = "Europe/Rome"
      }

      env {
        name  = "auth.app-log-level"
        value = var.rtp_activator_app_log_level
      }

      env {
        name        = "IDENTITY_CLIENT_ID"
        secret_name = "identity-client-id"
      }

      env {
        name        = "AZURE_CLIENT_ID"
        secret_name = "identity-client-id"
      }

      env {
        name  = "OTEL_SERVICE_NAME"
        value = "rtp-activator"
      }

      dynamic "env" {
        for_each = var.rtp_environment_configs
        content {
          name = env.key
          value = env.value
        }
      }

      dynamic "env" {
        for_each = var.rtp_environment_secrets
        content {
          name = env.key
          secret_name = replace(lower(env.key), "_", "-")
        }
      }
      
       volume_mounts {
        name    = "jks-volume"
        path    = "/mnt/jks"
      }

    }

    volume {
      name         = "jks-volume"
      storage_type = "AzureFile"
      storage_name = data.azurerm_storage_share.rtp_jks_file_share.name
    }

    max_replicas = var.rtp_activator_max_replicas
    min_replicas = var.rtp_activator_min_replicas
  }

  secret {
    name  = "identity-client-id"
    value = "${data.azurerm_user_assigned_identity.rtp-activator.client_id}"
  }

  secret {
    name = "azure-file-account-name"
    value = data.azurerm_storage_account.rtp_files_storage_account.name
  }

  secret {
    name = "azure-file-account-key" 
    value = data.azurerm_storage_account.rtp_files_storage_account.primary_access_key
  }

  dynamic "secret" {
    for_each = var.rtp_environment_secrets
    content {
      name = replace(lower(secret.key), "_", "-")
      key_vault_secret_id = "${data.azurerm_key_vault.rtp-kv.vault_uri}secrets/${secret.value}"
      identity            = data.azurerm_user_assigned_identity.rtp-activator.id
    }
  }

  identity {
    type = "UserAssigned"
    identity_ids = [data.azurerm_user_assigned_identity.rtp-activator.id]
  }

  ingress {
    external_enabled = true
    target_port      = 8080
    transport        = "http"

    traffic_weight {
      latest_revision = true
      percentage      = 100
      #revision_suffix = formatdate("YYYYMMDDhhmmssZZZZ", timestamp())
    }
  }

  tags = var.tags
}