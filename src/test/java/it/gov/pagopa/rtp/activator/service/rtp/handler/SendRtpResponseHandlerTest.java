package it.gov.pagopa.rtp.activator.service.rtp.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig.Activation;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig.Send;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig.Send.Retry;
import it.gov.pagopa.rtp.activator.domain.errors.SepaRequestException;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpRepository;
import it.gov.pagopa.rtp.activator.domain.rtp.TransactionStatus;
import it.gov.pagopa.rtp.activator.service.rtp.RtpStatusUpdater;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SendRtpResponseHandlerTest {

  private RtpStatusUpdater rtpStatusUpdater;
  private RtpRepository rtpRepository;
  private SendRtpResponseHandler sendRtpResponseHandler;


  @BeforeEach
  void setUp() {
    this.rtpStatusUpdater = mock(RtpStatusUpdater.class);
    this.rtpRepository = mock(RtpRepository.class);

    final var serviceProviderConfig = new ServiceProviderConfig(
        "http://localhost:8080",
        new Activation("http://localhost:8080"),
        new Send("v1", new Retry(3, 100, 0.75), 10000L));

    this.sendRtpResponseHandler = new SendRtpResponseHandler(rtpStatusUpdater, rtpRepository,
        serviceProviderConfig);
  }


  @ParameterizedTest
  @MethodSource("provideTransactionStatusAndExpectedMethodForSuccess")
  void givenValidRequest_whenTransactionStatusIsX_thenTriggerY(
      TransactionStatus transactionStatus,
      String expectedMethod)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    final var rtpToSend = mock(Rtp.class);
    final var request = new EpcRequest(rtpToSend, null, null, transactionStatus);

    when(
        rtpStatusUpdater.getClass()
        .getMethod(expectedMethod, Rtp.class)
        .invoke(rtpStatusUpdater, rtpToSend))
        .thenReturn(Mono.just(rtpToSend));

    when(rtpRepository.save(rtpToSend))
        .thenReturn(Mono.just(rtpToSend));

    final var result = sendRtpResponseHandler.handle(request);

    StepVerifier.create(result)
        .expectNextMatches(req -> req.rtpToSend().equals(rtpToSend))
        .verifyComplete();
  }

  private static Stream<Arguments> provideTransactionStatusAndExpectedMethodForSuccess() {
    return Stream.of(
        Arguments.of(TransactionStatus.ACTC, "triggerAcceptRtp"),
        Arguments.of(null, "triggerSendRtp")
    );
  }

  @ParameterizedTest
  @MethodSource("provideTransactionStatusForException")
  void givenValidRequest_whenTransactionStatusIsX_thenThrowException(
      TransactionStatus transactionStatus,
      Class<? extends Throwable> expectedException,
      String expectedMethod)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    final var rtpToSend = mock(Rtp.class);
    final var request = mock(EpcRequest.class);

    when(request.rtpToSend()).thenReturn(rtpToSend);
    when(request.response()).thenReturn(transactionStatus);

    if (expectedMethod != null) {
      when(
          rtpStatusUpdater.getClass()
              .getMethod(expectedMethod, Rtp.class)
              .invoke(rtpStatusUpdater, rtpToSend))
          .thenReturn(Mono.just(rtpToSend));

      when(rtpRepository.save(rtpToSend))
          .thenReturn(Mono.just(rtpToSend));
    }

    final var result = sendRtpResponseHandler.handle(request);

    StepVerifier.create(result)
        .expectErrorMatches(throwable -> throwable.getClass().equals(expectedException))
        .verify();
  }

  private static Stream<Arguments> provideTransactionStatusForException() {
    return Stream.of(
        Arguments.of(TransactionStatus.ACCP, IllegalStateException.class, null),
        Arguments.of(TransactionStatus.RJCT, SepaRequestException.class, "triggerRejectRtp"),
        Arguments.of(TransactionStatus.ERROR, SepaRequestException.class, "triggerErrorSendRtp")
    );
  }

  @Test
  void givenNullRequest_whenHandleCalled_thenThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> sendRtpResponseHandler.handle(null));
  }

}