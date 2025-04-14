package it.gov.pagopa.rtp.activator.domain.errors;

import java.util.UUID;

public class PayerAlreadyExists extends Throwable {
  private final UUID existingActivationId;
  private final String fiscalCode;
  private final String url;

  public PayerAlreadyExists() {
    super("Payer already exists");
    this.existingActivationId = null;
    this.fiscalCode = null;
    this.url = null;
  }

  public PayerAlreadyExists(UUID existingActivationId, String fiscalCode, String url) {
    super("Payer with fiscal code " + fiscalCode + " already exists");
    this.existingActivationId = existingActivationId;
    this.fiscalCode = fiscalCode;
    this.url = url;
  }

  public UUID getExistingActivationId() {
    return existingActivationId;
  }

  public String getFiscalCode() {
    return fiscalCode;
  }

  public String getUrl() {
    return url;
  }
}
