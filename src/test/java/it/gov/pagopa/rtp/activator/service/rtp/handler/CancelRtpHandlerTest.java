package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.configuration.OpenAPIClientFactory;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.configuration.mtlswebclient.WebClientFactory;
import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProviderFullData;
import it.gov.pagopa.rtp.activator.domain.registryfile.TechnicalServiceProvider;
import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.TransactionStatus;
import it.gov.pagopa.rtp.activator.epcClient.api.DefaultApi;
import it.gov.pagopa.rtp.activator.epcClient.invoker.ApiClient;
import it.gov.pagopa.rtp.activator.epcClient.model.SepaRequestToPayCancellationRequestResourceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.SynchronousRequestToPayCancellationResponseDto;
import it.gov.pagopa.rtp.activator.service.rtp.SepaRequestToPayMapper;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelRtpHandlerTest {

  private static final long MAX_ATTEMPTS = 3L;
  private static final long BACKOFF_MIN_DURATION = 100L;
  private static final double BACKOFF_JITTER = 0.5;

  @Mock
  private WebClientFactory webClientFactory;

  @Mock
  private OpenAPIClientFactory<DefaultApi> epcClientFactory;

  @Mock
  private SepaRequestToPayMapper sepaRequestToPayMapper;

  @Mock
  private ServiceProviderConfig serviceProviderConfig;

  @Mock
  private DefaultApi epcClient;

  @Mock
  private ApiClient apiClient;

  @Mock
  private ServiceProviderConfig.Send.Retry retryConfig;

  private CancelRtpHandler cancelRtpHandler;

  @BeforeEach
  void setUp() {
    lenient().when(serviceProviderConfig.send()).thenReturn(mock(ServiceProviderConfig.Send.class));
    lenient().when(serviceProviderConfig.send().retry()).thenReturn(retryConfig);
    lenient().when(retryConfig.maxAttempts()).thenReturn(MAX_ATTEMPTS);
    lenient().when(retryConfig.backoffMinDuration()).thenReturn(BACKOFF_MIN_DURATION);
    lenient().when(retryConfig.backoffJitter()).thenReturn(BACKOFF_JITTER);

    cancelRtpHandler = new CancelRtpHandler(webClientFactory, epcClientFactory, sepaRequestToPayMapper, serviceProviderConfig);
  }

  @Test
  void givenValidRequest_whenHandleRtpCancellation_thenCancelRtp() {
    final var transactionStatus = TransactionStatus.CNCL;
    final var resourceId = ResourceID.createNew();
    final var request = mock(EpcRequest.class);
    final var rtpToCancel = mock(Rtp.class);
    final var providerData = mock(ServiceProviderFullData.class);
    final var tsp = mock(TechnicalServiceProvider.class);
    final var sepaRequest = mock(SepaRequestToPayCancellationRequestResourceDto.class);
    final var sepaResponse = mock(SynchronousRequestToPayCancellationResponseDto.class);
    final var webClient = mock(WebClient.class);

    when(rtpToCancel.resourceID())
        .thenReturn(resourceId);
    when(request.rtpToSend())
        .thenReturn(rtpToCancel);
    when(request.serviceProviderFullData())
        .thenReturn(providerData);
    when(request.token())
        .thenReturn("token");
    when(providerData.tsp())
        .thenReturn(tsp);
    when(tsp.serviceEndpoint())
        .thenReturn("https://example.com");
    when(tsp.mtlsEnabled())
        .thenReturn(true);
    when(epcClientFactory.createClient(any()))
        .thenReturn(epcClient);
    when(epcClient.getApiClient())
        .thenReturn(apiClient);
    when(sepaRequestToPayMapper.toEpcRequestToCancel(rtpToCancel))
        .thenReturn(sepaRequest);
    when(epcClient.postRequestToPayCancellationRequest(any(), any(), any(), eq(sepaRequest)))
        .thenReturn(Mono.just(sepaResponse));
    when(request.withResponse(transactionStatus))
        .thenReturn(request);
    when(webClientFactory.createMtlsWebClient())
        .thenReturn(webClient);

    final var result = cancelRtpHandler.handle(request);

    StepVerifier.create(result)
        .expectNext(request)
        .verifyComplete();

    verify(epcClient).postRequestToPayCancellationRequest(any(), any(), any(), any());
  }

  @Test
  void givenRequestWithoutCertificate_whenHandleRtpCancellation_thenUseSimpleWebClient() {
    final var transactionStatus = TransactionStatus.CNCL;
    final var resourceId = ResourceID.createNew();
    final var request = mock(EpcRequest.class);
    final var rtpToCancel = mock(Rtp.class);
    final var providerData = mock(ServiceProviderFullData.class);
    final var tsp = mock(TechnicalServiceProvider.class);
    final var sepaRequest = mock(SepaRequestToPayCancellationRequestResourceDto.class);
    final var sepaResponse = mock(SynchronousRequestToPayCancellationResponseDto.class);
    final var webClient = mock(WebClient.class);

    when(rtpToCancel.resourceID())
        .thenReturn(resourceId);
    when(request.rtpToSend())
        .thenReturn(rtpToCancel);
    when(request.serviceProviderFullData())
        .thenReturn(providerData);
    when(providerData.tsp())
        .thenReturn(tsp);
    when(tsp.mtlsEnabled())
        .thenReturn(false);
    when(webClientFactory.createSimpleWebClient())
        .thenReturn(webClient);
    when(epcClientFactory.createClient(webClient))
        .thenReturn(epcClient);
    when(epcClient.getApiClient())
        .thenReturn(apiClient);
    when(sepaRequestToPayMapper.toEpcRequestToCancel(rtpToCancel))
        .thenReturn(sepaRequest);
    when(epcClient.postRequestToPayCancellationRequest(any(), any(), any(), eq(sepaRequest)))
        .thenReturn(Mono.just(sepaResponse));
    when(request.withResponse(transactionStatus))
        .thenReturn(request);

    final var result = cancelRtpHandler.handle(request);

    StepVerifier.create(result.log())
        .expectNext(request)
        .verifyComplete();

    verify(webClientFactory).createSimpleWebClient();
  }

  @Test
  void givenRequestFails_whenHandleRtpCancellation_thenRetry() {
    final var resourceId = ResourceID.createNew();
    final var request = mock(EpcRequest.class);
    final var rtpToCancel = mock(Rtp.class);
    final var providerData = mock(ServiceProviderFullData.class);
    final var tsp = mock(TechnicalServiceProvider.class);
    final var sepaRequest = mock(SepaRequestToPayCancellationRequestResourceDto.class);
    final var webClient = mock(WebClient.class);

    when(rtpToCancel.resourceID())
        .thenReturn(resourceId);
    when(request.rtpToSend())
        .thenReturn(rtpToCancel);
    when(request.serviceProviderFullData())
        .thenReturn(providerData);
    when(providerData.tsp())
        .thenReturn(tsp);
    when(tsp.serviceEndpoint())
        .thenReturn("https://example.com");
    when(webClientFactory.createSimpleWebClient())
        .thenReturn(webClient);
    when(epcClientFactory.createClient(webClient))
        .thenReturn(epcClient);
    when(epcClient.getApiClient())
        .thenReturn(apiClient);
    when(sepaRequestToPayMapper.toEpcRequestToCancel(rtpToCancel))
        .thenReturn(sepaRequest);
    when(epcClient.postRequestToPayCancellationRequest(any(), any(), any(), any()))
        .thenThrow(WebClientResponseException.create(500, "Simulated Failure", null, null, null));

    final var result = cancelRtpHandler.handle(request);

    StepVerifier.create(result)
        .expectError(RuntimeException.class)
        .verify();

    /*
     * Verify that the client is called MAX_ATTEMPTS times.
     * The +1 is needed to account for the initial deferred call:
     */
    verify(epcClient, times((int) MAX_ATTEMPTS + 1))
        .postRequestToPayCancellationRequest(any(), any(), any(), any());
  }


  @Test
  void givenValidRequest_whenCancellationFailsOnce_thenRetriesAndSucceeds() {
    final var transactionStatus = TransactionStatus.CNCL;
    final var resourceId = ResourceID.createNew();
    final var request = mock(EpcRequest.class);
    final var rtpToCancel = mock(Rtp.class);
    final var providerData = mock(ServiceProviderFullData.class);
    final var tsp = mock(TechnicalServiceProvider.class);
    final var sepaRequest = mock(SepaRequestToPayCancellationRequestResourceDto.class);
    final var sepaResponse = mock(SynchronousRequestToPayCancellationResponseDto.class);
    final var webClient = mock(WebClient.class);

    when(rtpToCancel.resourceID())
        .thenReturn(resourceId);
    when(request.rtpToSend())
        .thenReturn(rtpToCancel);
    when(request.serviceProviderFullData())
        .thenReturn(providerData);
    when(providerData.tsp())
        .thenReturn(tsp);
    when(tsp.serviceEndpoint())
        .thenReturn("https://example.com");
    when(webClientFactory.createSimpleWebClient())
        .thenReturn(webClient);
    when(epcClientFactory.createClient(webClient))
        .thenReturn(epcClient);
    when(epcClient.getApiClient())
        .thenReturn(apiClient);
    when(sepaRequestToPayMapper.toEpcRequestToCancel(rtpToCancel))
        .thenReturn(sepaRequest);

    final var shouldFail = new AtomicBoolean(true);
    when(epcClient.postRequestToPayCancellationRequest(any(), any(), any(), any()))
        .thenAnswer(
            invocation -> {
              if (shouldFail.getAndSet(false)) {
                throw new RuntimeException("Simulated call failure");
              }
              return Mono.just(sepaResponse);
            }
        );

    when(request.withResponse(transactionStatus))
        .thenReturn(request);

    final var result = cancelRtpHandler.handle(request);

    StepVerifier.create(result)
        .expectNext(request)
        .verifyComplete();
  }

  @Test
  void givenPartiallyFailingRtpSend_whenHandleRtpCancellation_thenRequestIdShouldChange() {
    final var transactionStatus = TransactionStatus.CNCL;
    final var numRetries = MAX_ATTEMPTS;
    final var resourceId = ResourceID.createNew();
    final var request = mock(EpcRequest.class);
    final var rtpToCancel = mock(Rtp.class);
    final var providerData = mock(ServiceProviderFullData.class);
    final var tsp = mock(TechnicalServiceProvider.class);
    final var sepaRequest = mock(SepaRequestToPayCancellationRequestResourceDto.class);
    final var sepaResponse = mock(SynchronousRequestToPayCancellationResponseDto.class);
    final var webClient = mock(WebClient.class);

    when(rtpToCancel.resourceID())
        .thenReturn(resourceId);
    when(request.rtpToSend())
        .thenReturn(rtpToCancel);
    when(request.serviceProviderFullData())
        .thenReturn(providerData);
    when(providerData.tsp())
        .thenReturn(tsp);
    when(tsp.serviceEndpoint())
        .thenReturn("https://example.com");
    when(tsp.mtlsEnabled())
        .thenReturn(true);
    when(epcClientFactory.createClient(any()))
        .thenReturn(epcClient);
    when(epcClient.getApiClient())
        .thenReturn(apiClient);
    when(sepaRequestToPayMapper.toEpcRequestToCancel(rtpToCancel))
        .thenReturn(sepaRequest);
    when(request.withResponse(transactionStatus))
        .thenReturn(request);
    when(webClientFactory.createMtlsWebClient())
        .thenReturn(webClient);

    final var retryCounter = new AtomicInteger();
    when(epcClient.postRequestToPayCancellationRequest(any(), any(), any(), any()))
        .thenAnswer(
            invocation -> {
              if (retryCounter.getAndIncrement() < numRetries - 1) {
                throw new RuntimeException("Simulated call failure");
              }
              return Mono.just(sepaResponse);
            }
        );

    StepVerifier.create(cancelRtpHandler.handle(request))
        .expectNext(request)
        .verifyComplete();

    final var requestIdCaptor = ArgumentCaptor.forClass(String.class);
    verify(epcClient, atLeast((int)numRetries))
        .postRequestToPayCancellationRequest(any(), requestIdCaptor.capture(), any(), any());

    final var capturedRequestIds = requestIdCaptor.getAllValues();
    assertEquals(numRetries, capturedRequestIds.size());
    assertEquals(numRetries, new HashSet<>(capturedRequestIds).size());
  }

  @Test
  void givenBadRequestResponse_whenHandleRtpCancellation_thenReturnRJCRStatus() {
    final var resourceId = ResourceID.createNew();
    final var request = mock(EpcRequest.class);
    final var rtpToCancel = mock(Rtp.class);
    final var providerData = mock(ServiceProviderFullData.class);
    final var tsp = mock(TechnicalServiceProvider.class);
    final var sepaRequest = mock(SepaRequestToPayCancellationRequestResourceDto.class);
    final var webClient = mock(WebClient.class);

    when(rtpToCancel.resourceID()).thenReturn(resourceId);
    when(request.rtpToSend()).thenReturn(rtpToCancel);
    when(request.serviceProviderFullData()).thenReturn(providerData);
    when(providerData.tsp()).thenReturn(tsp);
    when(tsp.serviceEndpoint()).thenReturn("https://example.com");
    when(webClientFactory.createSimpleWebClient()).thenReturn(webClient);
    when(epcClientFactory.createClient(webClient)).thenReturn(epcClient);
    when(epcClient.getApiClient()).thenReturn(apiClient);
    when(sepaRequestToPayMapper.toEpcRequestToCancel(rtpToCancel)).thenReturn(sepaRequest);

    WebClientResponseException cause = WebClientResponseException.create(
            400, "Bad Request", null, null, null);
    IllegalStateException retryExhausted = new IllegalStateException("Retry exhausted", cause);
    retryExhausted.addSuppressed(new RuntimeException("Retries finished"));

    when(epcClient.postRequestToPayCancellationRequest(any(), any(), any(), any()))
            .thenThrow(retryExhausted);
    when(request.withResponse(TransactionStatus.ERROR)).thenReturn(request);

    StepVerifier.create(cancelRtpHandler.handle(request))
            .expectNext(request)
            .verifyComplete();
  }

  @Test
  void givenGenericErrorResponse_whenHandleRtpCancellation_thenReturnErrorStatus() {
    final var resourceId = ResourceID.createNew();
    final var request = mock(EpcRequest.class);
    final var rtpToCancel = mock(Rtp.class);
    final var providerData = mock(ServiceProviderFullData.class);
    final var tsp = mock(TechnicalServiceProvider.class);
    final var sepaRequest = mock(SepaRequestToPayCancellationRequestResourceDto.class);
    final var webClient = mock(WebClient.class);

    when(rtpToCancel.resourceID()).thenReturn(resourceId);
    when(request.rtpToSend()).thenReturn(rtpToCancel);
    when(request.serviceProviderFullData()).thenReturn(providerData);
    when(providerData.tsp()).thenReturn(tsp);
    when(tsp.serviceEndpoint()).thenReturn("https://example.com");
    when(webClientFactory.createSimpleWebClient()).thenReturn(webClient);
    when(epcClientFactory.createClient(webClient)).thenReturn(epcClient);
    when(epcClient.getApiClient()).thenReturn(apiClient);
    when(sepaRequestToPayMapper.toEpcRequestToCancel(rtpToCancel)).thenReturn(sepaRequest);

    WebClientResponseException cause = WebClientResponseException.create(
            500, "Internal Server Error", null, null, null);
    IllegalStateException retryExhausted = new IllegalStateException("Retry exhausted", cause);
    retryExhausted.addSuppressed(new RuntimeException("Retries finished"));

    when(epcClient.postRequestToPayCancellationRequest(any(), any(), any(), any()))
            .thenThrow(retryExhausted);
    when(request.withResponse(TransactionStatus.ERROR)).thenReturn(request);

    StepVerifier.create(cancelRtpHandler.handle(request))
            .expectNext(request)
            .verifyComplete();
  }
}