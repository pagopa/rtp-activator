package it.gov.pagopa.rtp.activator.configuration;

import it.gov.pagopa.rtp.activator.activateClient.api.ReadApi;
import it.gov.pagopa.rtp.activator.activateClient.invoker.ApiClient;
import it.gov.pagopa.rtp.activator.epcClient.api.DefaultApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationConfig {

  @Bean
  public WebClient webClient() {
    return WebClientInterceptor.createWebClient(WebClient.builder());
  }

  @Bean("apiClientActivation")
  public ApiClient apiClient(WebClient webClient) {
    return new ApiClient(webClient);
  }

  @Bean
  public ReadApi readApi(@Qualifier("apiClientActivation") ApiClient apiClient) {
    return new ReadApi(apiClient);
  }

  @Bean
  public DefaultApi defaultApi() {
    return new DefaultApi();
  }
}
