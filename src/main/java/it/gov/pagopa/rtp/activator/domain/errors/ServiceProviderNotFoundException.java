package it.gov.pagopa.rtp.activator.domain.errors;


/**
 * Exception thrown when a service provider is not found.
 */
public class ServiceProviderNotFoundException extends RuntimeException {

  /**
   * Constructs a new {@link ServiceProviderNotFoundException} with the specified detail message.
   *
   * @param message the detail message, which is saved for later retrieval by the {@link Throwable#getMessage()} method
   */
  public ServiceProviderNotFoundException(String message) {
    super(message);
  }
}
