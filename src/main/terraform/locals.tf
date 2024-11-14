locals {
  #
  # Project label.
  #
  project = var.domain == "" ? "${var.prefix}-${var.env_short}" : "${var.prefix}-${var.env_short}-${var.domain}"
}