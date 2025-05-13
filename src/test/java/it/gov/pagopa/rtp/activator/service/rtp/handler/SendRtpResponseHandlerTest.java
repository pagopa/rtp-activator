package it.gov.pagopa.rtp.activator.service.rtp.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.TransactionStatus;
import it.gov.pagopa.rtp.activator.service.rtp.RtpStatusUpdater;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SendRtpResponseHandlerTest {

  @Mock
  private RtpStatusUpdater rtpStatusUpdater;

  @InjectMocks
  private SendRtpResponseHandler sendRtpResponseHandler;


  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
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

    final var result = sendRtpResponseHandler.handle(request);

    StepVerifier.create(result)
        .expectNextMatches(req -> req.rtpToSend().equals(rtpToSend))
        .verifyComplete();
  }

  private static Stream<Arguments> provideTransactionStatusAndExpectedMethodForSuccess() {
    return Stream.of(
        Arguments.of(TransactionStatus.ACTC, "triggerAcceptRtp"),
        Arguments.of(TransactionStatus.RJCT, "triggerRejectRtp"),
        Arguments.of(TransactionStatus.ERROR, "triggerErrorSendRtp"),
        Arguments.of(null, "triggerSendRtp")
    );
  }

  @ParameterizedTest
  @MethodSource("provideTransactionStatusForException")
  void givenValidRequest_whenTransactionStatusIsX_thenThrowException(
      TransactionStatus transactionStatus,
      Class<? extends Throwable> expectedException) {

    final var rtpToSend = mock(Rtp.class);
    EpcRequest request = mock(EpcRequest.class);

    when(request.rtpToSend()).thenReturn(rtpToSend);
    when(request.response()).thenReturn(transactionStatus);

    final var result = sendRtpResponseHandler.handle(request);

    StepVerifier.create(result)
        .expectErrorMatches(throwable -> throwable.getClass().equals(expectedException))
        .verify();

    verifyNoInteractions(rtpStatusUpdater);
  }

  private static Stream<Arguments> provideTransactionStatusForException() {
    return Stream.of(
        Arguments.of(TransactionStatus.ACCP, IllegalStateException.class)
    );
  }

  @Test
  void givenNullRequest_whenHandleCalled_thenThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> sendRtpResponseHandler.handle(null));
  }

}