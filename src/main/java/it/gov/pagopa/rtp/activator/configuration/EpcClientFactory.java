package it.gov.pagopa.rtp.activator.configuration;

import it.gov.pagopa.rtp.activator.epcClient.api.DefaultApi;
import it.gov.pagopa.rtp.activator.epcClient.invoker.ApiClient;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


@Component("epcClientFactory")
@Slf4j
public class EpcClientFactory implements OpenAPIClientFactory<DefaultApi> {

  private final ServiceProviderConfig serviceProviderConfig;


  public EpcClientFactory(
      @NonNull final ServiceProviderConfig serviceProviderConfig) {

    this.serviceProviderConfig = Objects.requireNonNull(serviceProviderConfig);
  }


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
