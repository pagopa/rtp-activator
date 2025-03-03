package it.gov.pagopa.rtp.activator.configuration;

import it.gov.pagopa.rtp.activator.activateClient.api.ReadApi;
import it.gov.pagopa.rtp.activator.activateClient.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationConfig {

  @Bean("webClient")
  @Primary
  public WebClient webClient() {
    return WebClientInterceptor.createWebClient(WebClient.builder());
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
