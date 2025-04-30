resource "azurerm_cosmosdb_mongo_database" "rtp" {
  name                = "rtp"
  resource_group_name = var.cosmosdb_resource_group
  account_name        = var.cosmosdb_account_name

  autoscale_settings {
    max_throughput = var.cosmosdb_throughput
  }

  lifecycle {
    prevent_destroy = true
  }
}

resource "azurerm_cosmosdb_mongo_collection" "rtps" {
  name                = "rtps"
  resource_group_name = var.cosmosdb_resource_group
  account_name        = var.cosmosdb_account_name
  database_name       = azurerm_cosmosdb_mongo_database.rtp.name
  shard_key = "_id"

  autoscale_settings {
    max_throughput = var.cosmosdb_throughput
  }

  index {
    keys = ["_id"]
    unique = true
  }

  lifecycle {
    prevent_destroy = true
  }
}

resource "azurerm_cosmosdb_mongo_collection" "activations" {
  name                = "activations"
  resource_group_name = var.cosmosdb_resource_group
  account_name        = var.cosmosdb_account_name
  database_name       = azurerm_cosmosdb_mongo_database.rtp.name
  shard_key = "_id"

  autoscale_settings {
    max_throughput = var.cosmosdb_throughput
  }

  index {
    keys = ["_id"]
    unique = true
  }

  lifecycle {
    prevent_destroy = true
  }
}
