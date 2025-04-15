package it.gov.pagopa.rtp.activator.configuration.mtlswebclient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.handler.ssl.SslContext;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig.Send;
import it.gov.pagopa.rtp.activator.configuration.ssl.SslContextFactory;
import it.gov.pagopa.rtp.activator.telemetry.OpenTelemetryWebClientFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
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
  private OpenTelemetryWebClientFilter openTelemetryFilter;

  @Mock
  private ExchangeFilterFunction telemetryExchangeFilter;

  private DefaultWebClientFactory mtlsWebClientFactory;

  @BeforeEach
  void setUp() {
    when(openTelemetryFilter.filter()).thenReturn(telemetryExchangeFilter);
    mtlsWebClientFactory = new DefaultWebClientFactory(sslContextFactory, config, openTelemetryFilter);
  }

  @Test
  void createMtlsWebClient_CreatesWebClientWithSslContext() {
    when(sslContextFactory.getSslContext()).thenReturn(sslContext);
    when(config.send()).thenReturn(new Send(null, null, 10000L));

    WebClient result = mtlsWebClientFactory.createMtlsWebClient();

    assertNotNull(result);
    verify(sslContextFactory).getSslContext();
    verify(openTelemetryFilter).filter();
  }
}