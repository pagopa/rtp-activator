package it.gov.pagopa.rtp.activator.domain.errors;

import java.util.UUID;

public class RtpNotFoundException extends RuntimeException {

  private static final String DEFAULT_ERROR_MESSAGE = "Rtp not found. Id: %s";


  public RtpNotFoundException(UUID id) {
    super(String.format(DEFAULT_ERROR_MESSAGE, id));
  }
}
