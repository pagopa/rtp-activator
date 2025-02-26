package it.gov.pagopa.rtp.activator.configuration;

/**
 * Defines a provider for SSL context properties.
 * <p>
 * Implementations of this interface supply the necessary SSL configuration details,
 * such as the keystore, password, and SSL protocol settings, required for initializing
 * an {@link javax.net.ssl.SSLContext}.
 * </p>
 * <p>
 * This abstraction allows different sources to provide SSL properties, such as
 * environment variables, configuration files, or secret management systems.
 * </p>
 */
public interface SslContextPropsProvider {

  /**
   * Retrieves the SSL context properties.
   *
   * @return an instance of {@link SslContextProps} containing SSL configuration details.
   */
  SslContextProps getSslContextProps();
}

