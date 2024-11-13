# ------------------------------------------------------------------------------
# General variables.
# ------------------------------------------------------------------------------
prefix         = "cstar"
env_short      = "p"
env            = "prod"
location       = "westeurope" # this will be "italynorth"
location_short = "weu"        # this will be "itn"
domain         = "rtp"

tags = {
  CreatedBy   = "Terraform"
  Environment = "prod"
  Owner       = "cstar"
  Source      = "https://github.com/pagopa/rtp_activator/tree/main/src/main/terraform"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
  Domain      = "rtp"
}

# ------------------------------------------------------------------------------
# External resources.
# ------------------------------------------------------------------------------
cae_name                       = "cstar-p-rtp-cae"
cae_resource_group_name        = "cstar-p-rtp-app-rg"


# ------------------------------------------------------------------------------
# Names of key vault secrets.
# ------------------------------------------------------------------------------


# ------------------------------------------------------------------------------
# Configuration of the microservice.
# ------------------------------------------------------------------------------
rtp_activator_app_log_level                     = "DEBUG"
rtp_activator_image                             = "ghcr.io/pagopa/rtp-activator:latest"
rtp_activator_cpu                               = 0.1
rtp_activator_memory                            = "300Mi"
rtp_activator_max_replicas                      = 5
rtp_activator_min_replicas                      = 1
rtp_activator_base_url                          = "https://mil-d-apim.azure-api.net/rtp_activator"