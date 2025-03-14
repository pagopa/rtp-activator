package it.gov.pagopa.rtp.activator.service.rtp.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtp.activator.domain.registryfile.OAuth2;
import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProviderFullData;
import it.gov.pagopa.rtp.activator.domain.registryfile.TechnicalServiceProvider;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.service.oauth.Oauth2TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class Oauth2HandlerTest {

  @InjectMocks
  private Oauth2Handler oauth2Handler;

  @Mock
  private Oauth2TokenService oauth2TokenService;

  @Mock
  private Environment environment;

  private static final String CLIENT_SECRET_ENV_VAR = "secret.value";
  private static final String ACCESS_TOKEN = "mock-access-token";


  @Test
  void givenRequestWithOauth2_whenHandle_thenRetrieveAccessToken() {

    final var rtpToSend = mock(Rtp.class);
    final var oauth2Data = mock(OAuth2.class);
    final var tspData = new TechnicalServiceProvider("tspId", "tspName", "tspUrl", "tspSecret",
        oauth2Data);
    final var serviceProviderData = new ServiceProviderFullData("spId", "spName", tspData);
    final var request = new EpcRequest(rtpToSend, serviceProviderData, null, null);

    when(oauth2Data.tokenEndpoint()).thenReturn("tokenEndpoint");
    when(oauth2Data.clientId()).thenReturn("clientId");
    when(oauth2Data.scope()).thenReturn("scope");
    when(oauth2Data.clientSecretEnvVar()).thenReturn(CLIENT_SECRET_ENV_VAR);

    when(environment.getProperty("client.secret.value")).thenReturn(CLIENT_SECRET_ENV_VAR);
    when(oauth2TokenService.getAccessToken(
        anyString(), anyString(), anyString(), anyString()))
        .thenReturn(Mono.just(ACCESS_TOKEN));

    // When
    var resultMono = oauth2Handler.handle(request);

    // Then
    StepVerifier.create(resultMono)
        .assertNext(updatedRequest -> assertEquals(ACCESS_TOKEN, updatedRequest.token()))
        .verifyComplete();

    verify(oauth2TokenService).getAccessToken(
        anyString(), anyString(), anyString(), anyString());
  }


  @Test
  void givenRequestWithoutOauth2_whenHandle_thenSkipTokenRetrieval() {
    final var rtpToSend = mock(Rtp.class);
    final var tspData = new TechnicalServiceProvider("tspId", "tspName", "tspUrl", "tspSecret",
        null);
    final var serviceProviderData = new ServiceProviderFullData("spId", "spName", tspData);
    final var request = new EpcRequest(rtpToSend, serviceProviderData, null, null);

    var resultMono = oauth2Handler.handle(request);

    StepVerifier.create(resultMono)
        .assertNext(updatedRequest -> assertSame(request, updatedRequest))
        .verifyComplete();

    verifyNoInteractions(oauth2TokenService);
  }


  @Test
  void givenTokenRetrievalFails_whenHandle_thenPropagateError() {
    final var rtpToSend = mock(Rtp.class);
    final var oauth2Data = mock(OAuth2.class);
    final var tspData = new TechnicalServiceProvider("tspId", "tspName", "tspUrl", "tspSecret",
        oauth2Data);
    final var serviceProviderData = new ServiceProviderFullData("spId", "spName", tspData);
    final var request = new EpcRequest(rtpToSend, serviceProviderData, null, null);

    when(oauth2Data.tokenEndpoint()).thenReturn("tokenEndpoint");
    when(oauth2Data.clientId()).thenReturn("clientId");
    when(oauth2Data.scope()).thenReturn("scope");
    when(oauth2Data.clientSecretEnvVar()).thenReturn(CLIENT_SECRET_ENV_VAR);

    when(environment.getProperty("client.secret.value")).thenReturn(CLIENT_SECRET_ENV_VAR);
    when(oauth2TokenService.getAccessToken(
        anyString(), anyString(), anyString(), anyString()))
        .thenReturn(Mono.error(new RuntimeException("Token retrieval failed")));

    var resultMono = oauth2Handler.handle(request);

    StepVerifier.create(resultMono)
        .expectErrorMatches(error -> error instanceof RuntimeException
            && error.getMessage().equals("Token retrieval failed"))
        .verify();

    verify(oauth2TokenService).getAccessToken(
        anyString(), anyString(), anyString(), anyString());
  }
}
