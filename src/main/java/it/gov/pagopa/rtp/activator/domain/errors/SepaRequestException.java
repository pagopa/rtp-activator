package it.gov.pagopa.rtp.activator.domain.errors;

public class SepaRequestException extends RuntimeException {

  public SepaRequestException(String message) {
    super(message);
  }
}
