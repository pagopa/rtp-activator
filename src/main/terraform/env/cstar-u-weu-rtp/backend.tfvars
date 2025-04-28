resource_group_name  = "terraform-state-rg"
storage_account_name = "tfappuatcstar"
container_name       = "terraform-state"
key                  = "rtp-activator.tfstate"
cosmosdb_resource_group = "cstar-u-weu-rtp-data-rg"
cosmosdb_account_name   = "cstar-u-weu-rtp-cosmos"
cosmosdb_throughput     = 400