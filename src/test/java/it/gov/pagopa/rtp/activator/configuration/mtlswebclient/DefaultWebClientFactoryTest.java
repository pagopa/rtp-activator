package it.gov.pagopa.rtp.activator.configuration.mtlswebclient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.handler.ssl.SslContext;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig.Send;
import it.gov.pagopa.rtp.activator.configuration.ssl.SslContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class DefaultWebClientFactoryTest {

  @Mock
  private SslContextFactory sslContextFactory;
  @Mock
  private ServiceProviderConfig config;

  @Mock
  private SslContext sslContext;

  @Mock
  private WebClient.Builder webClientBuilder;

  @Mock
  private WebClient webClient;

  private DefaultWebClientFactory mtlsWebClientFactory;

  @BeforeEach
  void setUp() {
    when(webClientBuilder.clientConnector(any())).thenReturn(webClientBuilder);
    when(webClientBuilder.build()).thenReturn(webClient);

    mtlsWebClientFactory = new DefaultWebClientFactory(sslContextFactory, config, webClientBuilder);
  }

  @Test
  void createMtlsWebClient_CreatesWebClientWithSslContext() {
    when(sslContextFactory.getSslContext()).thenReturn(sslContext);
    when(config.send()).thenReturn(new Send(null, null, 10000L));

    WebClient result = mtlsWebClientFactory.createMtlsWebClient();

    assertNotNull(result);
    verify(sslContextFactory).getSslContext();
    verify(webClientBuilder).clientConnector(any());
    verify(webClientBuilder).build();
  }
}