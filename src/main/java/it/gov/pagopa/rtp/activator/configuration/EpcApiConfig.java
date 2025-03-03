package it.gov.pagopa.rtp.activator.configuration;

import io.netty.handler.ssl.SslContext;
import it.gov.pagopa.rtp.activator.configuration.ssl.SslContextFactory;
import it.gov.pagopa.rtp.activator.epcClient.api.DefaultApi;
import it.gov.pagopa.rtp.activator.epcClient.invoker.ApiClient;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;


/**
 * Configuration class for setting up EPC API client with mTLS security.
 * <p>
 * This class is responsible for creating and configuring the required WebClient and API clients
 * with mutual TLS (mTLS) support. It retrieves the necessary SSL context from the provided
 * {@link SslContextFactory}.
 * </p>
 */
@Configuration
@Slf4j
public class EpcApiConfig {

  private final SslContext sslContext;


  /**
   * Constructs an instance of {@link EpcApiConfig} with the required SSL context.
   *
   * @param sslContextFactory       the factory used to create the SSL context.
   * @throws IllegalStateException  if the SSL context cannot be retrieved.
   */
  public EpcApiConfig(@NonNull final SslContextFactory sslContextFactory) {
    this.sslContext = Optional.of(sslContextFactory)
        .map(SslContextFactory::getSslContext)
        .orElseThrow(() -> new IllegalStateException("SSL context is null"));
  }


  /**
   * Creates a WebClient bean with mutual TLS (mTLS) security enabled.
   *
   * @return a configured {@link WebClient} instance.
   */
  @Bean("mTlsWebClient")
  @NonNull
  public WebClient mTlsWebClient() {
    log.trace("Creating mTLS web client");

    final var httpClient = HttpClient.create()
        .secure(ssl -> ssl.sslContext(this.sslContext)
            .build());

    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
  }


  /**
   * Creates an EPC API client bean that uses the mTLS-enabled WebClient.
   *
   * @param mTlsWebClient the {@link WebClient} instance configured with mTLS.
   * @return              a new {@link ApiClient} instance.
   */
  @Bean("epcApiClient")
  @NonNull
  public ApiClient apiClient(@Qualifier("mTlsWebClient") @NonNull final WebClient mTlsWebClient) {
    return new ApiClient(mTlsWebClient);
  }


  /**
   * Creates an instance of {@link DefaultApi}, the EPC API generated class configured with the
   * given API client and service provider configuration.
   *
   * @param apiClient               the {@link ApiClient} instance to use.
   * @param serviceProviderConfig   the configuration containing the EPC API details.
   * @return                        a configured {@link DefaultApi} instance.
   * @throws IllegalStateException  if EPC API client couldn't be created or the EPC API mock base
   *                                path is missing.
   * @throws NullPointerException   if required dependencies are null.
   */
  @Bean("epcApi")
  @NonNull
  public DefaultApi defaultApi(
      @Qualifier("epcApiClient") @NonNull final ApiClient apiClient,
      @NonNull final ServiceProviderConfig serviceProviderConfig) {

    Objects.requireNonNull(apiClient, "API client cannot be null");
    Objects.requireNonNull(serviceProviderConfig, "Service provider config cannot be null");

    log.trace("Creating EPC client");

    final var mockBasePath = Optional.of(serviceProviderConfig)
        .map(ServiceProviderConfig::send)
        .map(ServiceProviderConfig.Send::epcMockUrl)
        .orElseThrow(() -> new IllegalStateException("Couldn't create mock base path"));

    return Optional.of(apiClient)
        .map(DefaultApi::new)
        .map(defaultApi -> {
          defaultApi.getApiClient().setBasePath(mockBasePath);
          return defaultApi;
        })
        .orElseThrow(() -> new IllegalStateException("Couldn't create EPC client"));
  }
}

