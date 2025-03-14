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


@Component("defaultMtlsWebClientFactory")
public class DefaultWebClientFactory implements WebClientFactory {

  private final SslContextFactory sslContextFactory;
  private final ServiceProviderConfig serviceProviderConfig;

  public DefaultWebClientFactory(SslContextFactory sslContextFactory,
      ServiceProviderConfig serviceProviderConfig) {
    this.sslContextFactory = sslContextFactory;
    this.serviceProviderConfig = serviceProviderConfig;
  }


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


  @NonNull
  @Override
  public WebClient createMtlsWebClient() {
    HttpClient httpClient = HttpClient.create()
        .secure(sslContextSpec -> sslContextSpec.sslContext(sslContextFactory.getSslContext()))
        .responseTimeout(Duration.ofMillis(serviceProviderConfig.send().timeout()));

    return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }

}
