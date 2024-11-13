# ------------------------------------------------------------------------------
# Container Apps Environment.
# ------------------------------------------------------------------------------
data "azurerm_container_app_environment" "rtp-cae" {
  name                = var.cae_name
  resource_group_name = var.cae_resource_group_name
}
