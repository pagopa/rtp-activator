package it.gov.pagopa.rtp.activator.configuration;

import it.gov.pagopa.rtp.activator.epcClient.api.DefaultApi;
import it.gov.pagopa.rtp.activator.epcClient.invoker.ApiClient;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * A factory class for creating instances of the {@link DefaultApi} OpenAPI client.
 * This implementation uses a {@link WebClient} and configuration from {@link ServiceProviderConfig}.
 */
@Component("epcClientFactory")
@Slf4j
public class EpcClientFactory implements OpenAPIClientFactory<DefaultApi> {

  private final ServiceProviderConfig serviceProviderConfig;


  /**
   * Constructs an instance of {@link EpcClientFactory} with the specified service provider configuration.
   *
   * @param serviceProviderConfig the configuration used to set up the EPC client
   * @throws NullPointerException if the provided serviceProviderConfig is null
   */
  public EpcClientFactory(
      @NonNull final ServiceProviderConfig serviceProviderConfig) {

    this.serviceProviderConfig = Objects.requireNonNull(serviceProviderConfig);
  }


  /**
   * Creates an instance of the {@link DefaultApi} client using the provided {@link WebClient}.
   *
   * @param webClient the WebClient to be used for making API calls
   * @return an instance of {@link DefaultApi}
   * @throws IllegalStateException if the mock base path cannot be created or if the EPC client cannot be instantiated
   */
  @NonNull
  @Override
  public DefaultApi createClient(@NonNull final WebClient webClient) {
    log.debug("Creating EPC client");

    final var mockBasePath = Optional.of(serviceProviderConfig)
        .map(ServiceProviderConfig::send)
        .map(ServiceProviderConfig.Send::epcMockUrl)
        .orElseThrow(() -> new IllegalStateException("Couldn't create mock base path"));

    return Optional.of(webClient)
        .map(ApiClient::new)
        .map(DefaultApi::new)
        .map(defaultApi -> {
          defaultApi.getApiClient().setBasePath(mockBasePath);
          return defaultApi;
        })
        .orElseThrow(() -> new IllegalStateException("Couldn't create EPC client"));
  }
}
