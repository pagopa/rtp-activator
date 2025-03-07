package it.gov.pagopa.rtp.activator.configuration;

import it.gov.pagopa.rtp.activator.activateClient.api.ReadApi;
import it.gov.pagopa.rtp.activator.activateClient.invoker.ApiClient;
import it.gov.pagopa.rtp.activator.configuration.mtlswebclient.MtlsWebClientFactory;
import it.gov.pagopa.rtp.activator.epcClient.api.DefaultApi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationConfig {

  @Bean
  public WebClient webClient() {
    return WebClientInterceptor.createWebClient(WebClient.builder());
  }

  @Bean
  public ApiClient apiClient(WebClient webClient) {
    return new ApiClient(webClient);
  }

  @Bean
  public ReadApi readApi(ApiClient apiClient) {
    return new ReadApi(apiClient);
  }

  @Bean
  public DefaultApi defaultApi(ServiceProviderConfig serviceProviderConfig, MtlsWebClientFactory mtlsWebClientFactory) {
    var apiClient = new it.gov.pagopa.rtp.activator.epcClient.invoker.ApiClient(mtlsWebClientFactory.createMtlsWebClient());
    
    apiClient.setBasePath(serviceProviderConfig.send().epcMockUrl());

    return new DefaultApi(apiClient);

  }

}
