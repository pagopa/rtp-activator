package it.gov.pagopa.rtp.activator.statemachine;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.Objects;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;


@Component("rtpStateMachineFactory")
public class RtpStateMachineFactory implements StateMachineFactory<RtpEntity, RtpEvent> {

  private final TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> transitionConfigurer;


  public RtpStateMachineFactory(
      @NonNull final TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> transitionConfigurer) {
    this.transitionConfigurer = Objects.requireNonNull(transitionConfigurer);
  }

  @Override
  public StateMachine<RtpEntity, RtpEvent> createStateMachine() {
    return new RtpStateMachine(this.transitionConfigurer.build());
  }
}
