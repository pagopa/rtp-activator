# ------------------------------------------------------------------------------
# General variables.
# ------------------------------------------------------------------------------
prefix         = "cstar"
env_short      = "d"
env            = "dev"
location       = "westeurope" # this will be "italynorth"
location_short = "weu"        # this will be "itn"
domain         = "rtp"

tags = {
  CreatedBy   = "Terraform"
  Environment = "dev"
  Owner       = "cstar"
  Source      = "https://github.com/pagopa/rtp-activator/tree/main/src/main/terraform"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
  Domain      = "rtp"
}

# ------------------------------------------------------------------------------
# External resources.
# ------------------------------------------------------------------------------
cae_name                       = "cstar-d-tier-0-cae"
cae_resource_group_name        = "cstar-d-tier-0-app-rg"
id_name                        = "cstar-d-tier-0-auth-id"
id_resource_group_name         = "cstar-d-tier-0-identity-rg"

# ------------------------------------------------------------------------------
# Names of key vault secrets.
# ------------------------------------------------------------------------------


# ------------------------------------------------------------------------------
# Configuration of the microservice.
# ------------------------------------------------------------------------------
rtp_activator_app_log_level                     = "DEBUG"
rtp_activator_image                             = "ghcr.io/pagopa/rtp-activator:latest"
rtp_activator_cpu                               = 0.25
rtp_activator_memory                            = "0.5Gi"
rtp_activator_max_replicas                      = 5
rtp_activator_min_replicas                      = 1
rtp_activator_base_url                          = "https://mil-d-apim.azure-api.net/rtp-activator"