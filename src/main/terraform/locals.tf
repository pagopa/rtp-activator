locals {
  #
  # Project label.
  #
  project = var.domain == "" ? "${var.prefix}-${var.env_short}" : "${var.prefix}-${var.env_short}-${var.domain}"

  rtp_kv_name = "${var.prefix}-${var.env_short}-${var.location_short}-${var.domain}-kv"
  rtp_kv_resource_group_name = "${var.prefix}-${var.env_short}-${var.location_short}-${var.domain}-${var.location_short == "itn" ? "security" : "sec"}-rg"
}

