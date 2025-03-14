package it.gov.pagopa.rtp.activator.configuration;

import it.gov.pagopa.rtp.activator.activateClient.api.ReadApi;
import it.gov.pagopa.rtp.activator.activateClient.invoker.ApiClient;
import it.gov.pagopa.rtp.activator.configuration.mtlswebclient.WebClientFactory;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationConfig {

  @Bean("webClient")
  @Primary
  public WebClient webClient(@NonNull final WebClientFactory webClientFactory) {
    return Optional.of(webClientFactory)
        .map(WebClientFactory::createSimpleWebClient)
        .orElseThrow(() -> new IllegalStateException("Couldn't create web client"));
  }


  @Bean("activationApiClient")
  public ApiClient apiClient(WebClient webClient) {
    return new ApiClient(webClient);
  }


  @Bean("activationApi")
  public ReadApi readApi(ApiClient apiClient) {
    return new ReadApi(apiClient);
  }

}
