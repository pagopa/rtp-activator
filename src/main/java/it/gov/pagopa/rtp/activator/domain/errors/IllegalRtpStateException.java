package it.gov.pagopa.rtp.activator.domain.errors;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;

public class IllegalRtpStateException extends RuntimeException {

  public IllegalRtpStateException(RtpStatus status, String message) {
    super(message + " - Rtp status: " + status);
  }
}
