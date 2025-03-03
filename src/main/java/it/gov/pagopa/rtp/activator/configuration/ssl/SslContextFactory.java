package it.gov.pagopa.rtp.activator.configuration.ssl;

import io.netty.handler.ssl.SslContext;

/**
 * Factory interface for creating and providing an {@link SslContext}.
 * <p>
 * Implementations of this interface should handle the initialization and configuration of the
 * SSLContext, ensuring it is properly set up with the required cryptographic material such as key
 * stores and key managers.
 * </p>
 *
 * <p>
 * This factory can be used to obtain an SslContext that supports secure communication for various
 * network-based services requiring TLS encryption.
 * </p>
 */
public interface SslContextFactory {

  /**
   * Creates and returns a fully initialized {@link SslContext}.
   *
   * @return a configured {@link SslContext} instance.
   */
  SslContext getSslContext();

}

