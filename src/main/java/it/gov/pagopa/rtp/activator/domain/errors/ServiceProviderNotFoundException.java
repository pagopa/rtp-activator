package it.gov.pagopa.rtp.activator.domain.errors;

public class ServiceProviderNotFoundException extends RuntimeException {
  public ServiceProviderNotFoundException(String message) {
    super(message);
  }
}
