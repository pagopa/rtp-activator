package it.gov.pagopa.rtp.activator.configuration;

import it.gov.pagopa.rtp.activator.configuration.mtlswebclient.MtlsWebClientFactory;
import it.gov.pagopa.rtp.activator.epcClient.api.DefaultApi;
import it.gov.pagopa.rtp.activator.epcClient.invoker.ApiClient;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * Configuration class for setting up EPC API client with mTLS security.
 * <p>
 * This class is responsible for creating and configuring the EPC API clients
 * with mutual TLS (mTLS) support.
 * </p>
 */
@Configuration
@Slf4j
public class EpcApiConfig {


  /**
   * Creates an EPC API client bean that uses the mTLS-enabled WebClient.
   *
   * @param mtlsWebClientFactory a factory that produces a {@link WebClient} instance configured with mTLS.
   * @return                     a new {@link ApiClient} instance.
   */
  @Bean("epcApiClient")
  @NonNull
  public ApiClient apiClient(@NonNull final MtlsWebClientFactory mtlsWebClientFactory) {
    return new ApiClient(mtlsWebClientFactory.createMtlsWebClient());
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

