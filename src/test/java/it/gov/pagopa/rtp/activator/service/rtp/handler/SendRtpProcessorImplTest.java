package it.gov.pagopa.rtp.activator.service.rtp.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtp.activator.domain.registryfile.OAuth2;
import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProviderFullData;
import it.gov.pagopa.rtp.activator.domain.registryfile.TechnicalServiceProvider;
import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SendRtpProcessorImplTest {

  @Mock
  private RegistryDataHandler registryDataHandler;

  @Mock
  private Oauth2Handler oauth2Handler;

  @Mock
  private SendRtpHandler sendRtpHandler;

  @Mock
  private CancelRtpHandler cancelRtpHandler;

  @Mock
  private SendRtpResponseHandler sendRtpResponseHandler;

  @Mock
  private CancelRtpResponseHandler cancelRtpResponseHandler;

  @InjectMocks
  private SendRtpProcessorImpl sendRtpProcessor;

  @Test
  void givenValidRtp_whenSendRtpToServiceProviderDebtor_thenProcessSuccessfully() {
    final var spId = "spId";
    final var token = "token";
    final var resourceId = ResourceID.createNew();
    final var rtpToSend = mock(Rtp.class);
    final var oauth2Data = mock(OAuth2.class);
    final var tspData = new TechnicalServiceProvider("tspId", "tspName", "tspUrl", "tspSecret",
        oauth2Data, false);
    final var serviceProviderData = new ServiceProviderFullData(spId, "spName", tspData);
    final var inputEpcRequest = new EpcRequest(rtpToSend, null, null, null);
    final var epcRequestWithRegistryData = new EpcRequest(rtpToSend, serviceProviderData, null, null);
    final var epcRequestWithToken = new EpcRequest(rtpToSend, serviceProviderData, token, null);
    final var epcRequestWithResponse = new EpcRequest(rtpToSend, serviceProviderData, token, TransactionStatus.CNCL);

    when(rtpToSend.resourceID())
        .thenReturn(resourceId);
    when(rtpToSend.serviceProviderDebtor())
        .thenReturn(spId);
    when(registryDataHandler.handle(inputEpcRequest))
        .thenReturn(Mono.just(epcRequestWithRegistryData));
    when(oauth2Handler.handle(epcRequestWithRegistryData))
        .thenReturn(Mono.just(epcRequestWithToken));
    when(sendRtpHandler.handle(epcRequestWithToken))
        .thenReturn(Mono.just(epcRequestWithResponse));
    when(sendRtpResponseHandler.handle(epcRequestWithResponse))
        .thenReturn(Mono.just(epcRequestWithResponse));

    final var resultMono = sendRtpProcessor.sendRtpToServiceProviderDebtor(rtpToSend);

    StepVerifier.create(resultMono)
        .expectNext(rtpToSend)
        .verifyComplete();

    verify(registryDataHandler).handle(inputEpcRequest);
    verify(oauth2Handler).handle(epcRequestWithRegistryData);
    verify(sendRtpHandler).handle(epcRequestWithToken);
  }

  @Test
  void givenErrorInRegistryDataHandler_whenSendRtpToServiceProviderDebtor_thenHandleErrorGracefully() {
    final var rtpToSend = mock(Rtp.class);
    final var inputEpcRequest = new EpcRequest(rtpToSend, null, null, null);
    final var exception = new RuntimeException("Registry error");

    when(registryDataHandler.handle(inputEpcRequest)).thenReturn(Mono.error(exception));

    final var resultMono = sendRtpProcessor.sendRtpToServiceProviderDebtor(rtpToSend);

    StepVerifier.create(resultMono)
        .expectError(RuntimeException.class)
        .verify();

    verify(registryDataHandler).handle(inputEpcRequest);
    verifyNoInteractions(oauth2Handler, sendRtpHandler);
  }

  @Test
  void givenErrorInOauth2Handler_whenSendRtpToServiceProviderDebtor_thenHandleErrorGracefully() {
    final var spId = "spId";
    final var rtpToSend = mock(Rtp.class);
    final var oauth2Data = mock(OAuth2.class);
    final var tspData = new TechnicalServiceProvider("tspId", "tspName", "tspUrl", "tspSecret",
        oauth2Data, false);
    final var serviceProviderData = new ServiceProviderFullData(spId, "spName", tspData);
    final var inputEpcRequest = new EpcRequest(rtpToSend, null, null, null);
    final var epcRequestWithRegistryData = new EpcRequest(rtpToSend, serviceProviderData, null, null);
    final var exception = new RuntimeException("OAuth2 error");

    when(registryDataHandler.handle(inputEpcRequest))
        .thenReturn(Mono.just(epcRequestWithRegistryData));
    when(oauth2Handler.handle(epcRequestWithRegistryData))
        .thenReturn(Mono.error(exception));

    final var resultMono = sendRtpProcessor.sendRtpToServiceProviderDebtor(rtpToSend);

    StepVerifier.create(resultMono)
        .expectError(RuntimeException.class)
        .verify();

    verify(registryDataHandler).handle(inputEpcRequest);
    verify(oauth2Handler).handle(epcRequestWithRegistryData);
    verifyNoInteractions(sendRtpHandler);
  }

  @Test
  void givenErrorInSendRtpHandler_whenSendRtpToServiceProviderDebtor_thenHandleErrorGracefully() {
    final var spId = "spId";
    final var token = "token";
    final var rtpToSend = mock(Rtp.class);
    final var oauth2Data = mock(OAuth2.class);
    final var tspData = new TechnicalServiceProvider("tspId", "tspName", "tspUrl", "tspSecret",
        oauth2Data, false);
    final var serviceProviderData = new ServiceProviderFullData(spId, "spName", tspData);
    final var inputEpcRequest = new EpcRequest(rtpToSend, null, null, null);
    final var epcRequestWithRegistryData = new EpcRequest(rtpToSend, serviceProviderData, null, null);
    final var epcRequestWithToken = new EpcRequest(rtpToSend, serviceProviderData, token, null);
    final var exception = new RuntimeException("Send RTP error");

    when(registryDataHandler.handle(inputEpcRequest))
        .thenReturn(Mono.just(epcRequestWithRegistryData));
    when(oauth2Handler.handle(epcRequestWithRegistryData))
        .thenReturn(Mono.just(epcRequestWithToken));
    when(sendRtpHandler.handle(epcRequestWithToken))
        .thenReturn(Mono.error(exception));

    final var resultMono = sendRtpProcessor.sendRtpToServiceProviderDebtor(rtpToSend);

    StepVerifier.create(resultMono)
        .expectError(RuntimeException.class)
        .verify();

    verify(registryDataHandler).handle(inputEpcRequest);
    verify(oauth2Handler).handle(epcRequestWithRegistryData);
    verify(sendRtpHandler).handle(epcRequestWithToken);
  }

  @Test
  void givenEmptyResponseFromSendRtpHandler_whenSendRtpToServiceProviderDebtor_thenReturnOriginalRtp() {
    final var spId = "spId";
    final var token = "token";
    final var resourceId = ResourceID.createNew();
    final var rtpToSend = mock(Rtp.class);
    final var oauth2Data = mock(OAuth2.class);
    final var tspData = new TechnicalServiceProvider("tspId", "tspName", "tspUrl", "tspSecret",
        oauth2Data, false);
    final var serviceProviderData = new ServiceProviderFullData(spId, "spName", tspData);
    final var inputEpcRequest = new EpcRequest(rtpToSend, null, null, null);
    final var epcRequestWithRegistryData = new EpcRequest(rtpToSend, serviceProviderData, null, null);
    final var epcRequestWithToken = new EpcRequest(rtpToSend, serviceProviderData, token, null);

    when(rtpToSend.resourceID())
        .thenReturn(resourceId);
    when(registryDataHandler.handle(inputEpcRequest))
        .thenReturn(Mono.just(epcRequestWithRegistryData));
    when(oauth2Handler.handle(epcRequestWithRegistryData))
        .thenReturn(Mono.just(epcRequestWithToken));
    when(sendRtpHandler.handle(epcRequestWithToken))
        .thenReturn(Mono.empty());

    final var resultMono = sendRtpProcessor.sendRtpToServiceProviderDebtor(rtpToSend);

    StepVerifier.create(resultMono)
        .expectNext(rtpToSend)
        .verifyComplete();

    verify(registryDataHandler).handle(inputEpcRequest);
    verify(oauth2Handler).handle(epcRequestWithRegistryData);
    verify(sendRtpHandler).handle(epcRequestWithToken);
  }


  @Test
  void givenValidRtp_whenSendRtpCancellationToServiceProviderDebtor_thenProcessSuccessfully() {
    final var spId = "spId";
    final var token = "token";
    final var resourceId = ResourceID.createNew();
    final var rtpToSend = mock(Rtp.class);
    final var oauth2Data = mock(OAuth2.class);
    final var tspData = new TechnicalServiceProvider("tspId", "tspName", "tspUrl", "tspSecret",
        oauth2Data, false);
    final var serviceProviderData = new ServiceProviderFullData(spId, "spName", tspData);
    final var inputEpcRequest = new EpcRequest(rtpToSend, null, null, null);
    final var epcRequestWithRegistryData = new EpcRequest(rtpToSend, serviceProviderData, null, null);
    final var epcRequestWithToken = new EpcRequest(rtpToSend, serviceProviderData, token, null);
    final var epcRequestWithResponse = new EpcRequest(rtpToSend, serviceProviderData, token, TransactionStatus.CNCL);

    when(rtpToSend.resourceID())
        .thenReturn(resourceId);
    when(rtpToSend.serviceProviderDebtor())
        .thenReturn(spId);
    when(registryDataHandler.handle(inputEpcRequest))
        .thenReturn(Mono.just(epcRequestWithRegistryData));
    when(oauth2Handler.handle(epcRequestWithRegistryData))
        .thenReturn(Mono.just(epcRequestWithToken));
    when(cancelRtpHandler.handle(epcRequestWithToken))
        .thenReturn(Mono.just(epcRequestWithResponse));
    when(cancelRtpResponseHandler.handle(epcRequestWithResponse))
        .thenReturn(Mono.just(epcRequestWithResponse));

    final var resultMono = sendRtpProcessor.sendRtpCancellationToServiceProviderDebtor(rtpToSend);

    StepVerifier.create(resultMono)
        .expectNext(rtpToSend)
        .verifyComplete();

    verify(registryDataHandler).handle(inputEpcRequest);
    verify(oauth2Handler).handle(epcRequestWithRegistryData);
    verify(cancelRtpHandler).handle(epcRequestWithToken);
    verify(cancelRtpResponseHandler).handle(epcRequestWithResponse);
  }


  @Test
  void givenErrorInRegistryDataHandler_whenSendRtpCancellationToServiceProviderDebtor_thenHandleErrorGracefully() {
    final var rtpToSend = mock(Rtp.class);
    final var inputEpcRequest = new EpcRequest(rtpToSend, null, null, null);
    final var exception = new RuntimeException("Registry error");

    when(registryDataHandler.handle(inputEpcRequest)).thenReturn(Mono.error(exception));

    final var resultMono = sendRtpProcessor.sendRtpCancellationToServiceProviderDebtor(rtpToSend);

    StepVerifier.create(resultMono)
        .expectError(RuntimeException.class)
        .verify();

    verify(registryDataHandler).handle(inputEpcRequest);
    verifyNoInteractions(oauth2Handler, cancelRtpHandler);
    verifyNoInteractions(cancelRtpResponseHandler);
  }


  @Test
  void givenErrorInOauth2Handler_whenSendRtpCancellationToServiceProviderDebtor_thenHandleErrorGracefully() {
    final var spId = "spId";
    final var rtpToSend = mock(Rtp.class);
    final var oauth2Data = mock(OAuth2.class);
    final var tspData = new TechnicalServiceProvider("tspId", "tspName", "tspUrl", "tspSecret",
        oauth2Data, false);
    final var serviceProviderData = new ServiceProviderFullData(spId, "spName", tspData);
    final var inputEpcRequest = new EpcRequest(rtpToSend, null, null, null);
    final var epcRequestWithRegistryData = new EpcRequest(rtpToSend, serviceProviderData, null, null);
    final var exception = new RuntimeException("OAuth2 error");

    when(registryDataHandler.handle(inputEpcRequest))
        .thenReturn(Mono.just(epcRequestWithRegistryData));
    when(oauth2Handler.handle(epcRequestWithRegistryData))
        .thenReturn(Mono.error(exception));

    final var resultMono = sendRtpProcessor.sendRtpCancellationToServiceProviderDebtor(rtpToSend);

    StepVerifier.create(resultMono)
        .expectError(RuntimeException.class)
        .verify();

    verify(registryDataHandler).handle(inputEpcRequest);
    verify(oauth2Handler).handle(epcRequestWithRegistryData);
    verifyNoInteractions(cancelRtpHandler);
    verifyNoInteractions(cancelRtpResponseHandler);
  }


  @Test
  void givenErrorInSendRtpHandler_whenSendRtpCancellationToServiceProviderDebtor_thenHandleErrorGracefully() {
    final var spId = "spId";
    final var token = "token";
    final var rtpToSend = mock(Rtp.class);
    final var oauth2Data = mock(OAuth2.class);
    final var tspData = new TechnicalServiceProvider("tspId", "tspName", "tspUrl", "tspSecret",
        oauth2Data, false);
    final var serviceProviderData = new ServiceProviderFullData(spId, "spName", tspData);
    final var inputEpcRequest = new EpcRequest(rtpToSend, null, null, null);
    final var epcRequestWithRegistryData = new EpcRequest(rtpToSend, serviceProviderData, null, null);
    final var epcRequestWithToken = new EpcRequest(rtpToSend, serviceProviderData, token, null);
    final var exception = new RuntimeException("Send RTP error");

    when(registryDataHandler.handle(inputEpcRequest))
        .thenReturn(Mono.just(epcRequestWithRegistryData));
    when(oauth2Handler.handle(epcRequestWithRegistryData))
        .thenReturn(Mono.just(epcRequestWithToken));
    when(cancelRtpHandler.handle(epcRequestWithToken))
        .thenReturn(Mono.error(exception));

    final var resultMono = sendRtpProcessor.sendRtpCancellationToServiceProviderDebtor(rtpToSend);

    StepVerifier.create(resultMono)
        .expectError(RuntimeException.class)
        .verify();

    verify(registryDataHandler).handle(inputEpcRequest);
    verify(oauth2Handler).handle(epcRequestWithRegistryData);
    verify(cancelRtpHandler).handle(epcRequestWithToken);
    verifyNoInteractions(cancelRtpResponseHandler);
  }

  @Test
  void givenEmptyResponseFromSendRtpHandler_whenSendRtpCancellationToServiceProviderDebtor_thenReturnOriginalRtp() {
    final var spId = "spId";
    final var token = "token";
    final var resourceId = ResourceID.createNew();
    final var rtpToSend = mock(Rtp.class);
    final var oauth2Data = mock(OAuth2.class);
    final var tspData = new TechnicalServiceProvider("tspId", "tspName", "tspUrl", "tspSecret",
        oauth2Data, false);
    final var serviceProviderData = new ServiceProviderFullData(spId, "spName", tspData);
    final var inputEpcRequest = new EpcRequest(rtpToSend, null, null, null);
    final var epcRequestWithRegistryData = new EpcRequest(rtpToSend, serviceProviderData, null, null);
    final var epcRequestWithToken = new EpcRequest(rtpToSend, serviceProviderData, token, null);

    when(rtpToSend.resourceID())
        .thenReturn(resourceId);
    when(registryDataHandler.handle(inputEpcRequest))
        .thenReturn(Mono.just(epcRequestWithRegistryData));
    when(oauth2Handler.handle(epcRequestWithRegistryData))
        .thenReturn(Mono.just(epcRequestWithToken));
    when(cancelRtpHandler.handle(epcRequestWithToken))
        .thenReturn(Mono.empty());

    final var resultMono = sendRtpProcessor.sendRtpCancellationToServiceProviderDebtor(rtpToSend);

    StepVerifier.create(resultMono)
        .expectNext(rtpToSend)
        .verifyComplete();

    verify(registryDataHandler).handle(inputEpcRequest);
    verify(oauth2Handler).handle(epcRequestWithRegistryData);
    verify(cancelRtpHandler).handle(epcRequestWithToken);
    verifyNoInteractions(cancelRtpResponseHandler);
  }

}
