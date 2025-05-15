package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.errors.RtpInvalidStateTransition;
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
  void givenValidRtpAndStatusCNCL_whenHandle_thenCancelAccrTriggeredAndEventAdded() {
    Rtp rtp = createRtpWithStatus();
    TransactionStatus status = TransactionStatus.CNCL;
    EpcRequest request = new EpcRequest(rtp, null, null, status);

    Rtp updated = cloneWithSameEvents(rtp);
    Rtp finalUpdated = cloneWithSameEvents(updated);

    when(updater.triggerCancelRtp(rtp)).thenReturn(Mono.just(updated));
    when(updater.triggerCancelRtpAccr(updated)).thenReturn(Mono.just(finalUpdated));

    StepVerifier.create(handler.handle(request))
                .expectNextMatches(resp -> {
              Rtp resultRtp = resp.rtpToSend();
              return resultRtp.events().stream()
                  .anyMatch(e -> e.triggerEvent() == RtpEvent.CANCEL_RTP_ACCR);
            })
        .verifyComplete();

    verify(updater).triggerCancelRtp(rtp);
    verify(updater).triggerCancelRtpAccr(updated);
  }

  @Test
  void givenValidRtpAndStatusRJCR_whenHandle_thenCancelRejectedTriggeredAndEventAdded() {
    Rtp rtp = createRtpWithStatus();
    TransactionStatus status = TransactionStatus.RJCR;
    EpcRequest request = new EpcRequest(rtp, null, null, status);

    Rtp updated = cloneWithSameEvents(rtp);
    Rtp finalUpdated = cloneWithSameEvents(updated);

    when(updater.triggerCancelRtp(rtp)).thenReturn(Mono.just(updated));
    when(updater.triggerCancelRtpRejected(updated)).thenReturn(Mono.just(finalUpdated));

    StepVerifier.create(handler.handle(request))
                .expectNextMatches(resp -> resp.rtpToSend().events().stream()
                    .anyMatch(e -> e.triggerEvent() == RtpEvent.CANCEL_RTP_REJECTED))
        .verifyComplete();

    verify(updater).triggerCancelRtp(rtp);
    verify(updater).triggerCancelRtpRejected(updated);
  }

  @Test
  void givenValidRtpAndNullStatus_whenHandle_thenOnlyTriggerCancelAndReturn() {
    Rtp rtp = createRtpWithStatus();
    EpcRequest request = new EpcRequest(rtp, null, null, null);

    Rtp updated = cloneWithSameEvents(rtp);

    when(updater.triggerCancelRtp(rtp)).thenReturn(Mono.just(updated));

    StepVerifier.create(handler.handle(request))
        .expectNextMatches(resp -> resp.rtpToSend().status() == RtpStatus.CANCELLED)
        .verifyComplete();

    verify(updater).triggerCancelRtp(rtp);
    verifyNoMoreInteractions(updater);
  }

  @Test
    void givenTriggerCancelFailsWithIllegalStateException_whenHandle_thenMapToRtpInvalidStateTransition() {
    Rtp rtp = createRtpWithStatus();
    EpcRequest request = new EpcRequest(rtp, null, null, null);

    when(updater.triggerCancelRtp(rtp)).thenReturn(Mono.error(new IllegalStateException("Invalid state")));

    StepVerifier.create(handler.handle(request))
        .expectError(RtpInvalidStateTransition.class)
        .verify();
  }

  @Test
  void givenValidRtpAndStatusERROR_whenHandle_thenErrorCancelTriggeredAndEventAdded() {
    Rtp rtp = createRtpWithStatus();
    TransactionStatus status = TransactionStatus.ERROR;
    EpcRequest request = new EpcRequest(rtp, null, null, status);

    Rtp updated = cloneWithSameEvents(rtp);
    Rtp finalUpdated = cloneWithSameEvents(updated);

    when(updater.triggerCancelRtp(rtp)).thenReturn(Mono.just(updated));
    when(updater.triggerErrorCancelRtp(updated)).thenReturn(Mono.just(finalUpdated));

    StepVerifier.create(handler.handle(request))
            .expectNextMatches(resp -> resp.rtpToSend().events().stream()
                    .anyMatch(e -> e.triggerEvent() == RtpEvent.ERROR_CANCEL_RTP))
            .verifyComplete();

    verify(updater).triggerCancelRtp(rtp);
    verify(updater).triggerErrorCancelRtp(updated);
  }

  @Test
  void givenUnsupportedTransactionStatus_whenHandle_thenThrowsIllegalStateException() {
    Rtp rtp = createRtpWithStatus();
    TransactionStatus unsupported = TransactionStatus.ACTC;

    EpcRequest request = new EpcRequest(rtp, null, null, unsupported);

    when(updater.triggerCancelRtp(rtp)).thenReturn(Mono.just(rtp));

    StepVerifier.create(handler.handle(request))
        .expectErrorMatches(
            err ->
                err instanceof IllegalStateException
                    && err.getMessage().contains("TransactionStatus not supported"))
        .verify();
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

  private Rtp cloneWithSameEvents(Rtp original) {

    return new Rtp(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        new ResourceID(original.resourceID().getId()),
        null,
        null,
        null,
        null,
        null,
        null,
            RtpStatus.CANCELLED,
        null,
        new ArrayList<>(original.events()));
  }
}
