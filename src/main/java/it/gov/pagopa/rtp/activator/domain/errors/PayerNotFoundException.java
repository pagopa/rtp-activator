package it.gov.pagopa.rtp.activator.domain.errors;

public class PayerNotFoundException extends RuntimeException {

  public PayerNotFoundException() {
    super("The payer is not activated.");
  }

  public PayerNotFoundException(String message) {
    super(message);
  }

  public PayerNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
