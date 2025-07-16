# ------------------------------------------------------------------------------
# General variables.
# ------------------------------------------------------------------------------
prefix         = "cstar"
env_short      = "d"
env            = "dev"
location       = "italynorth"
location_short = "itn"
domain         = "srtp"

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
cae_name                       = "cstar-d-itn-srtp-cae"
cae_resource_group_name        = "cstar-d-itn-srtp-compute-rg"
id_name                        = "cstar-d-itn-srtp-activator-id"
id_resource_group_name         = "cstar-d-itn-srtp-identity-rg"

# ------------------------------------------------------------------------------
# Configuration of the microservice.
# ------------------------------------------------------------------------------
rtp_activator_app_log_level                     = "DEBUG"
rtp_activator_image                             = "ghcr.io/pagopa/rtp-activator:latest"
rtp_activator_cpu                               = 0.25
rtp_activator_memory                            = "0.5Gi"
rtp_activator_max_replicas                      = 5
rtp_activator_min_replicas                      = 1

rtp_environment_secrets = {
  COSMOS_ACCOUNT_RTP_CONNECTION_STRING  : "cosmosdb-account-rtp-primary-connection-string"
  APPLICATIONINSIGHTS_CONNECTION_STRING : "appinsights-connection-string"
  CLIENT_CERTIFICATE                    : "client-certificate"
  CLIENT_SECRET_CBI                     : "client-secret-cbi"
}


rtp_environment_configs = {
  DB_NAME                                 : "activation"
  BASE_URL                                : "https://api-rtp.dev.cstar.pagopa.it/rtp/activation/activations/"
  SP_BASE_URL                             : "https://api-rtp.dev.cstar.pagopa.it/rtp/rtps/"
  OTEL_TRACES_SAMPLER                     : "always_on"
  EPC_MOCK_URL                            : "https://api-rtp.dev.cstar.pagopa.it/rtp/mock"
  EPC_SEND_RETRY_MAX_ATTEMPTS             : 1
  EPC_SEND_RETRY_BACKOFF_MIN_DURATION_MS  : 1000
  EPC_SEND_RETRY_BACKOFF_JITTER           : 0.75
  EPC_SEND_TIMEOUT_MS                     : 6000
  AZURE_STORAGE_ACCOUNT_NAME              : "cstarditnsrtpsa"
  AZURE_STORAGE_CONTAINER_NAME            : "rtp-debtor-service-provider"
  AZURE_BLOB_NAME                         : "serviceregistry.json"
  CALLBACK_BASE_URL                       : "https://api-rtp-cb.dev.cstar.pagopa.it/rtp/cb"
}

