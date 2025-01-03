package it.gov.pagopa.rtp.activator.domain.errors;

public class PayerNotActivatedException extends RuntimeException {

  public PayerNotActivatedException() {
    super("The payer is not activated.");
  }

}
