package it.gov.pagopa.rtp.activator.configuration.mtlswebclient;

import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import java.time.Duration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import it.gov.pagopa.rtp.activator.configuration.ssl.SslContextFactory;
import reactor.netty.http.client.HttpClient;


/**
 * Default implementation of the {@link WebClientFactory} interface.
 * <p>
 * This class provides methods to create both a standard {@link WebClient}
 * and a mutual TLS (mTLS) secured {@link WebClient}, based on the
 * configured service provider settings.
 */
@Component("defaultMtlsWebClientFactory")
public class DefaultWebClientFactory implements WebClientFactory {

  private final SslContextFactory sslContextFactory;
  private final ServiceProviderConfig serviceProviderConfig;

  /**
   * Constructs an instance of {@code DefaultWebClientFactory}.
   *
   * @param sslContextFactory       the factory responsible for creating SSL contexts
   *                                used for secure mTLS connections
   * @param serviceProviderConfig   configuration settings for the service provider,
   *                                including timeout configurations
   */
  public DefaultWebClientFactory(SslContextFactory sslContextFactory,
      ServiceProviderConfig serviceProviderConfig) {
    this.sslContextFactory = sslContextFactory;
    this.serviceProviderConfig = serviceProviderConfig;
  }

  /**
   * Creates a simple {@link WebClient} instance without mutual TLS (mTLS).
   * <p>
   * The created {@link WebClient} instance is configured with a response timeout
   * based on the service provider's settings and includes a bearer token filter
   * for authentication.
   *
   * @return a non-mTLS configured {@link WebClient} instance
   */
  @NonNull
  @Override
  public WebClient createSimpleWebClient() {
    final var httpClient = HttpClient.create()
        .responseTimeout(Duration.ofMillis(serviceProviderConfig.send().timeout()));

    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .filter(new ServerBearerExchangeFilterFunction())
        .build();
  }

  /**
   * Creates a mutual TLS (mTLS) enabled {@link WebClient} instance.
   * <p>
   * The created {@link WebClient} is configured to use an SSL context
   * provided by {@link SslContextFactory}, ensuring secure client authentication
   * via mutual TLS.
   *
   * @return an mTLS-configured {@link WebClient} instance
   */
  @NonNull
  @Override
  public WebClient createMtlsWebClient() {
    HttpClient httpClient = HttpClient.create()
        .secure(sslContextSpec -> sslContextSpec.sslContext(sslContextFactory.getSslContext()))
        .responseTimeout(Duration.ofMillis(serviceProviderConfig.send().timeout()));

    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
  }
}

