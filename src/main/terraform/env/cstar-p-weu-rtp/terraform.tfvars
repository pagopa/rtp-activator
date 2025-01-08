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
cae_name                       = "cstar-p-mcshared-cae"
cae_resource_group_name        = "cstar-p-mcshared-app-rg"
id_name                        = "cstar-p-weu-rtp-activator-id"
id_resource_group_name         = "cstar-p-weu-rtp-identity-rg"

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
rtp_activator_base_url                          = "https://mil-d-apim.azure-api.net/rtp_activator"

rtp_environment_secrets = {
  COSMOS_ACCOUNT_RTP_CONNECTION_STRING  : "cosmosdb-account-rtp-connection-string"
  APPLICATIONINSIGHTS_CONNECTION_STRING : "appinsights-connection-string"
}

rtp_environment_configs = {
  DB_NAME                   : "rtp"
  BASE_URL                  : "https://api-rtp.cstar.pagopa.it/activations/"
  OTEL_TRACES_SAMPLER       : "always_on"
}
