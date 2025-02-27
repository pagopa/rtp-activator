package it.gov.pagopa.rtp.activator.configuration.MtlsWebClient;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import reactor.netty.http.client.HttpClient;

public class DefaultMtlsWebClientFactory implements MtlsWebClientFactory {
  private final SslContext sslContext;

  public DefaultMtlsWebClientFactory(SslContext sslContext) {
    this.sslContext = sslContext;
  }

  @Override
  public WebClient createMtlsWebClient() {
    HttpClient httpClient = HttpClient.create().secure(sslContexSpec -> sslContexSpec.sslContext(sslContext));

    return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }

}
