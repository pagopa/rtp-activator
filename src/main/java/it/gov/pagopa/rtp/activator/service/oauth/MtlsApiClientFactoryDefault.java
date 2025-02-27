package it.gov.pagopa.rtp.activator.service.oauth;


import reactor.netty.http.client.HttpClient;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import it.gov.pagopa.rtp.activator.epcClient.invoker.ApiClient;

public class MtlsApiClientFactoryDefault implements MtlsApiClientFactory {

  private final SslContext sslContextFactory;

  public MtlsApiClientFactoryDefault(SslContext sslContextFactory) {
    this.sslContextFactory = sslContextFactory;
  }

  @Override
  public ApiClient createMtlsApiClient(String basePath) {
    SslContext sslContext = sslContextFactory;

    HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

    WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
        .baseUrl(basePath).build();

    ApiClient apiClient = new ApiClient(webClient);

    apiClient.setBasePath(basePath);

    return apiClient;
  }

}
