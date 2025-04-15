package it.gov.pagopa.rtp.activator.domain.errors;

import java.util.UUID;

public class PayerAlreadyExists extends Throwable {
  private final UUID existingActivationId;
  private final String fiscalCode;

  public PayerAlreadyExists() {
    super("Payer already exists");
    this.existingActivationId = null;
    this.fiscalCode = null;
  }

  public PayerAlreadyExists(UUID existingActivationId, String fiscalCode) {
    super("Payer with fiscal code " + fiscalCode + " already exists");
    this.existingActivationId = existingActivationId;
    this.fiscalCode = fiscalCode;
  }

  public UUID getExistingActivationId() {
    return existingActivationId;
  }

  public String getFiscalCode() {
    return fiscalCode;
  }
}
