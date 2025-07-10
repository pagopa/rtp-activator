package it.gov.pagopa.rtp.activator.domain.errors;

import java.util.UUID;

public class PayerNotFoundException extends RuntimeException {
  private static final String DEFAULT_ERROR_MESSAGE = "Payer not found with activationId: %s";

  public PayerNotFoundException(UUID id) {
    super(String.format(DEFAULT_ERROR_MESSAGE, id));
  }
}
