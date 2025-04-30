package it.gov.pagopa.rtp.activator.service.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpMapper;
import it.gov.pagopa.rtp.activator.statemachine.StateMachine;
import it.gov.pagopa.rtp.activator.statemachine.StateMachineFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class RtpStatusUpdaterImplTest {

  @Mock
  private StateMachineFactory<RtpEntity, RtpEvent> stateMachineFactory;

  @Mock
  private StateMachine<RtpEntity, RtpEvent> stateMachine;

  @Mock
  private RtpMapper rtpMapper;

  @Mock
  private Rtp rtp;

  @Mock
  private RtpEntity rtpEntity;

  @InjectMocks
  private RtpStatusUpdaterImpl rtpStatusUpdater;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    when(stateMachineFactory.createStateMachine()).
        thenReturn(stateMachine);
    when(rtpMapper.toDbEntity(rtp))
        .thenReturn(rtpEntity);
    when(stateMachine.transition(eq(rtpEntity), any()))
        .thenReturn(Mono.just(rtpEntity));
    when(rtpMapper.toDomain(rtpEntity))
        .thenReturn(rtp);

    rtpStatusUpdater = new RtpStatusUpdaterImpl(stateMachineFactory, rtpMapper);
  }

  @Test
  void givenRtp_whenTriggerSendRtp_thenTransitionAndReturn() {
    StepVerifier.create(rtpStatusUpdater.triggerSendRtp(rtp))
        .expectNext(rtp)
        .verifyComplete();

    verify(stateMachine).transition(rtpEntity, RtpEvent.SEND_RTP);
  }

  @Test
  void givenRtp_whenTriggerCancelRtp_thenTransitionAndReturn() {
    StepVerifier.create(rtpStatusUpdater.triggerCancelRtp(rtp))
        .expectNext(rtp)
        .verifyComplete();

    verify(stateMachine).transition(rtpEntity, RtpEvent.CANCEL_RTP);
  }

  @Test
  void givenRtp_whenTriggerAcceptRtp_thenTransitionAndReturn() {
    StepVerifier.create(rtpStatusUpdater.triggerAcceptRtp(rtp))
        .expectNext(rtp)
        .verifyComplete();

    verify(stateMachine).transition(rtpEntity, RtpEvent.ACCEPT_RTP);
  }

  @Test
  void givenRtp_whenTriggerRejectRtp_thenTransitionAndReturn() {
    StepVerifier.create(rtpStatusUpdater.triggerRejectRtp(rtp))
        .expectNext(rtp)
        .verifyComplete();

    verify(stateMachine).transition(rtpEntity, RtpEvent.REJECT_RTP);
  }

  @Test
  void givenRtp_whenTriggerUserAcceptRtp_thenTransitionAndReturn() {
    StepVerifier.create(rtpStatusUpdater.triggerUserAcceptRtp(rtp))
        .expectNext(rtp)
        .verifyComplete();

    verify(stateMachine).transition(rtpEntity, RtpEvent.USER_ACCEPT_RTP);
  }

  @Test
  void givenRtp_whenTriggerUserRejectRtp_thenTransitionAndReturn() {
    StepVerifier.create(rtpStatusUpdater.triggerUserRejectRtp(rtp))
        .expectNext(rtp)
        .verifyComplete();

    verify(stateMachine).transition(rtpEntity, RtpEvent.USER_REJECT_RTP);
  }

  @Test
  void givenRtp_whenTriggerPayRtp_thenTransitionAndReturn() {
    StepVerifier.create(rtpStatusUpdater.triggerPayRtp(rtp))
        .expectNext(rtp)
        .verifyComplete();

    verify(stateMachine).transition(rtpEntity, RtpEvent.PAY_RTP);
  }

  @Test
  void givenRtp_whenTriggerErrorSendRtp_thenTransitionAndReturn() {
    StepVerifier.create(rtpStatusUpdater.triggerErrorSendRtp(rtp))
        .expectNext(rtp)
        .verifyComplete();

    verify(stateMachine).transition(rtpEntity, RtpEvent.ERROR_SEND_RTP);
  }

  @Test
  void givenRtp_whenTriggerErrorCancelRtp_thenTransitionAndReturn() {
    StepVerifier.create(rtpStatusUpdater.triggerErrorCancelRtp(rtp))
        .expectNext(rtp)
        .verifyComplete();

    verify(stateMachine).transition(rtpEntity, RtpEvent.ERROR_CANCEL_RTP);
  }

  @Test
  void givenRtp_whenTriggerCancelRtpAccr_thenTransitionAndReturn() {
    StepVerifier.create(rtpStatusUpdater.triggerCancelRtpAccr(rtp))
        .expectNext(rtp)
        .verifyComplete();

    verify(stateMachine).transition(rtpEntity, RtpEvent.CANCEL_RTP_ACCR);
  }

  @Test
  void givenRtp_whenTriggerCancelRtpRejected_thenTransitionAndReturn() {
    StepVerifier.create(rtpStatusUpdater.triggerCancelRtpRejected(rtp))
        .expectNext(rtp)
        .verifyComplete();

    verify(stateMachine).transition(rtpEntity, RtpEvent.CANCEL_RTP_REJECTED);
  }
}
