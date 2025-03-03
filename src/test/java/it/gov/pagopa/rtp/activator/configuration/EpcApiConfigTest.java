package it.gov.pagopa.rtp.activator.configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.netty.handler.ssl.SslContext;
import it.gov.pagopa.rtp.activator.configuration.ssl.SslContextFactory;
import it.gov.pagopa.rtp.activator.epcClient.invoker.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class EpcApiConfigTest {

  @Mock
  private SslContextFactory sslContextFactory;

  @Mock
  private SslContext sslContext;

  @Mock
  private WebClient webClient;

  @Mock
  private ApiClient apiClient;

  @Mock
  private ServiceProviderConfig serviceProviderConfig;

  @Mock
  private ServiceProviderConfig.Send sendConfig;

  private EpcApiConfig epcApiConfig;

  @BeforeEach
  void setUp() {
    when(sslContextFactory.getSslContext()).thenReturn(sslContext);

    epcApiConfig = new EpcApiConfig(sslContextFactory);
  }

  @Test
  void givenValidSslContext_whenCreatingWebClient_thenReturnConfiguredWebClient() {
    final var mTlsWebClient = epcApiConfig.mTlsWebClient();

    assertNotNull(mTlsWebClient, "WebClient should not be null");
  }

  @Test
  void givenWebClient_whenCreatingApiClient_thenReturnApiClient() {
    final var createdApiClient = epcApiConfig.apiClient(webClient);

    assertNotNull(createdApiClient, "ApiClient should not be null");
  }

  @Test
  void givenValidConfig_whenCreatingDefaultApi_thenReturnDefaultApi() {
    final var basePath = "https://mock-epc-url.com";

    when(serviceProviderConfig.send()).thenReturn(sendConfig);
    when(sendConfig.epcMockUrl()).thenReturn(basePath);
    when(apiClient.getBasePath()).thenReturn(basePath);

    final var defaultApi = epcApiConfig.defaultApi(apiClient, serviceProviderConfig);

    assertNotNull(defaultApi, "DefaultApi should not be null");
    assertEquals(basePath, defaultApi.getApiClient().getBasePath(),
        "Base path should match the provided mock URL");
  }

  @Test
  void givenNullServiceProviderConfig_whenCreatingDefaultApi_thenThrowException() {
    assertThrows(NullPointerException.class,
        () -> epcApiConfig.defaultApi(apiClient, null),
        "Should throw NullPointerException if ServiceProviderConfig is null");
  }

  @Test
  void givenNullApiClient_whenCreatingDefaultApi_thenThrowException() {
    assertThrows(NullPointerException.class,
        () -> epcApiConfig.defaultApi(null, serviceProviderConfig),
        "Should throw NullPointerException if ApiClient is null");
  }

  @Test
  void givenNullMockUrl_whenCreatingDefaultApi_thenThrowException() {
    when(serviceProviderConfig.send()).thenReturn(sendConfig);
    when(sendConfig.epcMockUrl()).thenReturn(null);

    assertThrows(IllegalStateException.class,
        () -> epcApiConfig.defaultApi(apiClient, serviceProviderConfig),
        "Should throw IllegalStateException if mock base path is null");
  }

  @Test
  void givenNullSslContext_whenCreatingConfig_thenThrowException() {
    when(sslContextFactory.getSslContext()).thenReturn(null);

    assertThrows(IllegalStateException.class,
        () -> new EpcApiConfig(sslContextFactory),
        "Should throw IllegalStateException if SSL context is null");
  }
}
