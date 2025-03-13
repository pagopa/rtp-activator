package it.gov.pagopa.rtp.activator.configuration.mtlswebclient;

import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import java.time.Duration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import it.gov.pagopa.rtp.activator.configuration.ssl.SslContextFactory;
import reactor.netty.http.client.HttpClient;

@Component
public class DefaultMtlsWebClientFactory implements MtlsWebClientFactory {
  private final SslContextFactory sslContextFactory;
  private final ServiceProviderConfig serviceProviderConfig;

  public DefaultMtlsWebClientFactory(SslContextFactory sslContextFactory,
      ServiceProviderConfig serviceProviderConfig) {
    this.sslContextFactory = sslContextFactory;
    this.serviceProviderConfig = serviceProviderConfig;
  }

  @Override
  public WebClient createMtlsWebClient() {
    HttpClient httpClient = HttpClient.create()
        .secure(sslContextSpec -> sslContextSpec.sslContext(sslContextFactory.getSslContext()))
        .responseTimeout(Duration.ofMillis(serviceProviderConfig.send().timeout()));

    return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }

}
