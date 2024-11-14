# ------------------------------------------------------------------------------
# Container Apps Environment.
# ------------------------------------------------------------------------------
data "azurerm_container_app_environment" "rtp-cae" {
  name                = var.cae_name
  resource_group_name = var.cae_resource_group_name
}

# ------------------------------------------------------------------------------
# Identity for this Container App.
# ------------------------------------------------------------------------------
data "azurerm_user_assigned_identity" "rtp-activator" {
  name                = var.id_name
  resource_group_name = var.id_resource_group_name
}
