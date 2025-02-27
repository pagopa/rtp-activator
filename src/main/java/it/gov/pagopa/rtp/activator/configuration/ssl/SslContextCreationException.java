package it.gov.pagopa.rtp.activator.configuration.ssl;

public class SslContextCreationException extends RuntimeException {

  public SslContextCreationException(String message) {
    super(message);
  }

  public SslContextCreationException(Throwable cause) {
    super(cause);
  }
}
