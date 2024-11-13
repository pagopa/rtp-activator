# ------------------------------------------------------------------------------
# Container app.
# ------------------------------------------------------------------------------
resource "azurerm_container_app" "rtp-activator" {
  name                         = "${local.project}-auth-ca"
  container_app_environment_id = data.azurerm_container_app_environment.rtp-cae.id
  resource_group_name          = data.azurerm_container_app_environment.rtp-cae.resource_group_name
  revision_mode                = "Single"

  template {
    container {
      name   = "rtp-activator"
      image  = var.mil_auth_image
      cpu    = var.mil_auth_cpu
      memory = var.mil_auth_memory

      env {
        name  = "TZ"
        value = "Europe/Rome"
      }

      env {
        name  = "auth.app-log-level"
        value = var.mil_auth_app_log_level
      }

      env {
        name        = "IDENTITY_CLIENT_ID"
        secret_name = "identity-client-id"
      }
    }

    max_replicas = var.mil_auth_max_replicas
    min_replicas = var.mil_auth_min_replicas
  }

  secret {
    name  = "identity-client-id"
    value = "${data.azurerm_user_assigned_identity.auth.client_id}"
  }

  identity {
    type = "UserAssigned"
    identity_ids = [data.azurerm_user_assigned_identity.auth.id]
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