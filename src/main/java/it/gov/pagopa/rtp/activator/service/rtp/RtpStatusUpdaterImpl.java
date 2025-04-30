package it.gov.pagopa.rtp.activator.service.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpMapper;
import it.gov.pagopa.rtp.activator.statemachine.StateMachine;
import it.gov.pagopa.rtp.activator.statemachine.StateMachineFactory;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component("rtpStatusUpdater")
@Slf4j
public class RtpStatusUpdaterImpl implements RtpStatusUpdater {

  private final StateMachine<RtpEntity, RtpEvent> stateMachine;
  private final RtpMapper rtpMapper;


  public RtpStatusUpdaterImpl(
      @NonNull final StateMachineFactory<RtpEntity, RtpEvent> stateMachineFactory,
      @NonNull final RtpMapper rtpMapper) {

    this.stateMachine = Objects.requireNonNull(stateMachineFactory)
        .createStateMachine();
    this.rtpMapper = Objects.requireNonNull(rtpMapper);
  }


  @NonNull
  @Override
  public Mono<Rtp> triggerSendRtp(@NonNull final Rtp rtp) {
    return this.triggerEvent(rtp, RtpEvent.SEND_RTP);
  }


  @NonNull
  @Override
  public Mono<Rtp> triggerCancelRtp(@NonNull final Rtp rtp) {
    return this.triggerEvent(rtp, RtpEvent.CANCEL_RTP);
  }


  @NonNull
  @Override
  public Mono<Rtp> triggerAcceptRtp(@NonNull final Rtp rtp) {
    return this.triggerEvent(rtp, RtpEvent.ACCEPT_RTP);
  }


  @NonNull
  @Override
  public Mono<Rtp> triggerRejectRtp(@NonNull final Rtp rtp) {
    return this.triggerEvent(rtp, RtpEvent.REJECT_RTP);
  }


  @NonNull
  @Override
  public Mono<Rtp> triggerUserAcceptRtp(@NonNull final Rtp rtp) {
    return this.triggerEvent(rtp, RtpEvent.USER_ACCEPT_RTP);
  }


  @NonNull
  @Override
  public Mono<Rtp> triggerUserRejectRtp(@NonNull final Rtp rtp) {
    return this.triggerEvent(rtp, RtpEvent.USER_REJECT_RTP);
  }

  @NonNull
  @Override
  public Mono<Rtp> triggerPayRtp(@NonNull final Rtp rtp) {
    return this.triggerEvent(rtp, RtpEvent.PAY_RTP);
  }


  @NonNull
  @Override
  public Mono<Rtp> triggerErrorSendRtp(@NonNull final Rtp rtp) {
    return this.triggerEvent(rtp, RtpEvent.ERROR_SEND_RTP);
  }


  @NonNull
  @Override
  public Mono<Rtp> triggerErrorCancelRtp(@NonNull final Rtp rtp) {
    return this.triggerEvent(rtp, RtpEvent.ERROR_CANCEL_RTP);
  }


  @NonNull
  @Override
  public Mono<Rtp> triggerCancelRtpAccr(@NonNull final Rtp rtp) {
    return this.triggerEvent(rtp, RtpEvent.CANCEL_RTP_ACCR);
  }


  @NonNull
  @Override
  public Mono<Rtp> triggerCancelRtpRejected(@NonNull final Rtp rtp) {
    return this.triggerEvent(rtp, RtpEvent.CANCEL_RTP_REJECTED);
  }


  @NonNull
  private Mono<Rtp> triggerEvent(
      @NonNull final Rtp sourceRtp, @NonNull final RtpEvent event) {

    Objects.requireNonNull(sourceRtp, "Rtp cannot be null");
    Objects.requireNonNull(event, "Event cannot be null");

    return Mono.just(sourceRtp)
        .doFirst(() -> log.debug("Triggering event {} for RTP status {}", event, sourceRtp.status()))
        .doOnNext(rtp -> log.debug("Mapping RTP model to RTP entity."))
        .map(this.rtpMapper::toDbEntity)
        .doOnNext(rtp -> log.debug("Calling state machine."))
        .flatMap(rtpEntity ->
            Mono.from(this.stateMachine.transition(rtpEntity, event)))
        .doOnNext(rtp -> log.debug("Mapping RTP entity to RTP model."))
        .map(this.rtpMapper::toDomain);
  }
}
