package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.rtp.*;
import it.gov.pagopa.rtp.activator.service.rtp.RtpStatusUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelRtpResponseHandlerTest {

  @Mock
  private RtpStatusUpdater updater;

  private CancelRtpResponseHandler handler;

  @BeforeEach
  void setUp() {
    handler = new CancelRtpResponseHandler(updater);
  }

  @Test
  void givenValidRtpAndStatusCNCL_whenHandle_thenCancelAccrTriggered() {
    Rtp rtp = createRtpWithStatus();
    TransactionStatus status = TransactionStatus.CNCL;
    EpcRequest request = new EpcRequest(rtp, null, null, status);

    Rtp updated = cancelRtpWithSameEvents(rtp);
    Rtp finalUpdated = cancelRtpWithSameEvents(updated);

    when(updater.triggerCancelRtp(rtp)).thenReturn(Mono.just(updated));
    when(updater.triggerCancelRtpAccr(updated)).thenReturn(Mono.just(finalUpdated));

    StepVerifier.create(handler.handle(request))
            .expectNextMatches(resp -> resp.rtpToSend().status() == RtpStatus.CANCELLED)
            .verifyComplete();

    verify(updater).triggerCancelRtp(rtp);
    verify(updater).triggerCancelRtpAccr(updated);
  }

  @Test
  void givenValidRtpAndStatusRJCR_whenHandle_thenCancelRejectedTriggered() {
    Rtp rtp = createRtpWithStatus();
    TransactionStatus status = TransactionStatus.RJCR;
    EpcRequest request = new EpcRequest(rtp, null, null, status);

    Rtp updated = cancelRtpWithSameEvents(rtp);
    Rtp finalUpdated = cancelRtpWithSameEvents(updated);

    when(updater.triggerCancelRtp(rtp)).thenReturn(Mono.just(updated));
    when(updater.triggerCancelRtpRejected(updated)).thenReturn(Mono.just(finalUpdated));

    StepVerifier.create(handler.handle(request))
            .expectNextMatches(resp -> resp.rtpToSend().status() == RtpStatus.CANCELLED)
            .verifyComplete();

    verify(updater).triggerCancelRtp(rtp);
    verify(updater).triggerCancelRtpRejected(updated);
  }

  @Test
  void givenValidRtpAndNullStatus_whenHandle_thenOnlyTriggerCancelAndReturn() {
    Rtp rtp = createRtpWithStatus();
    EpcRequest request = new EpcRequest(rtp, null, null, null);

    Rtp updated = cancelRtpWithSameEvents(rtp);

    when(updater.triggerCancelRtp(rtp)).thenReturn(Mono.just(updated));

    StepVerifier.create(handler.handle(request))
        .expectNextMatches(resp -> resp.rtpToSend().status() == RtpStatus.CANCELLED)
        .verifyComplete();

    verify(updater).triggerCancelRtp(rtp);
    verifyNoMoreInteractions(updater);
  }

  @Test
  void whenTriggerCancelRtpFails_thenPropagatesIllegalStateException() {
    Rtp rtp = createRtpWithStatus();
    EpcRequest request = new EpcRequest(rtp, null, null, null);

    when(updater.triggerCancelRtp(rtp)).thenReturn(Mono.error(new IllegalStateException("Invalid state")));

    StepVerifier.create(handler.handle(request))
            .expectErrorMatches(err -> err instanceof IllegalStateException &&
                    err.getMessage().equals("Invalid state"))
            .verify();

    verify(updater).triggerCancelRtp(rtp);
  }

  @Test
  void givenValidRtpAndStatusERROR_whenHandle_thenErrorCancelTriggered() {
    Rtp rtp = createRtpWithStatus();
    TransactionStatus status = TransactionStatus.ERROR;
    EpcRequest request = new EpcRequest(rtp, null, null, status);

    Rtp updated = cancelRtpWithSameEvents(rtp);
    Rtp finalUpdated = cancelRtpWithSameEvents(updated);

    when(updater.triggerCancelRtp(rtp)).thenReturn(Mono.just(updated));
    when(updater.triggerErrorCancelRtp(updated)).thenReturn(Mono.just(finalUpdated));

    StepVerifier.create(handler.handle(request))
            .expectNextMatches(resp -> resp.rtpToSend().status() == RtpStatus.CANCELLED)
            .verifyComplete();

    verify(updater).triggerCancelRtp(rtp);
    verify(updater).triggerErrorCancelRtp(updated);
  }

  @Test
  void whenUnsupportedTransactionStatus_thenPropagatesIllegalStateException() {
    Rtp rtp = createRtpWithStatus();
    TransactionStatus unsupportedStatus = TransactionStatus.ACTC;
    EpcRequest request = new EpcRequest(rtp, null, null, unsupportedStatus);

    when(updater.triggerCancelRtp(rtp)).thenReturn(Mono.just(rtp));

    StepVerifier.create(handler.handle(request))
            .expectErrorMatches(err -> err instanceof IllegalStateException &&
                    err.getMessage().contains("TransactionStatus not supported"))
            .verify();

    verify(updater).triggerCancelRtp(rtp);
  }

  private Rtp createRtpWithStatus() {
    return new Rtp(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        new ResourceID(UUID.randomUUID()),
        null,
        null,
        null,
        null,
        null,
        null,
            RtpStatus.SENT,
        null,
        new ArrayList<>());
  }

  private Rtp cancelRtpWithSameEvents(Rtp original) {
    return original
            .withStatus(RtpStatus.CANCELLED)
            .withEvents(new ArrayList<>(original.events()));
  }
}
