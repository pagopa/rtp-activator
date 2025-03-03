package it.gov.pagopa.rtp.activator.configuration.ssl;

import java.util.Objects;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;


/**
 * Provides SSL context properties retrieved from environment variables or external sources.
 * <p>
 * This implementation of {@link SslContextPropsProvider} ensures that the necessary SSL-related
 * properties, such as the keystore and password, are available for configuring an
 * {@link javax.net.ssl.SSLContext}.
 * </p>
 *
 * <p>
 * The SSL properties are typically injected via a configuration mechanism, allowing for flexible
 * and secure management of cryptographic material.
 * </p>
 */
@Component("envVarsSslContextPropsProvider")
public class EnvVarsSslContextPropsProvider implements SslContextPropsProvider {

  private final SslContextProps sslContextProps;


  /**
   * Constructs an instance of {@code EnvVarsSslContextPropsProvider} with the given SSL
   * properties.
   *
   * @param sslContextProps the SSL context properties to be provided.
   * @throws NullPointerException if {@code sslContextProps} is {@code null}.
   */
  public EnvVarsSslContextPropsProvider(@NonNull final SslContextProps sslContextProps) {
    this.sslContextProps = Objects.requireNonNull(sslContextProps);
  }


  /**
   * Retrieves the SSL context properties.
   *
   * @return the configured {@link SslContextProps} instance.
   */
  @NonNull
  @Override
  public SslContextProps getSslContextProps() {
    return this.sslContextProps;
  }
}

