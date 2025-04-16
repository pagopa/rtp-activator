package it.gov.pagopa.rtp.activator.domain.errors;

import java.util.UUID;

public class PayerAlreadyExists extends Throwable {
  private final UUID existingActivationId;

  public PayerAlreadyExists() {
    super("Payer already exists");
    this.existingActivationId = null;
  }

  public PayerAlreadyExists(UUID existingActivationId) {
    super("Payer with "+existingActivationId.toString()+" activation id already exists.");
    this.existingActivationId = existingActivationId;
  }

  public UUID getExistingActivationId() {
    return existingActivationId;
  }

}
