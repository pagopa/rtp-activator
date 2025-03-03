package it.gov.pagopa.rtp.activator.configuration.MtlsWebClient;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import it.gov.pagopa.rtp.activator.configuration.ssl.SslContextFactory;
import reactor.netty.http.client.HttpClient;

@Component
public class DefaultMtlsWebClientFactory implements MtlsWebClientFactory {
  private final SslContextFactory sslContextFactory;

  public DefaultMtlsWebClientFactory(SslContextFactory sslContextFactory) {
    this.sslContextFactory = sslContextFactory;
  }

  @Override
  public WebClient createMtlsWebClient() {
    HttpClient httpClient = HttpClient.create()
        .secure(sslContexSpec -> sslContexSpec.sslContext(sslContextFactory.getSslContext()));

    return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }

}
