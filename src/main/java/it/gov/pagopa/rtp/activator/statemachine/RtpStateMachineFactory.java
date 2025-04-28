package it.gov.pagopa.rtp.activator.statemachine;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.Objects;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;


/**
 * Factory component for creating instances of {@link RtpStateMachine}.
 * <p>
 * This class is responsible for building a new {@link StateMachine} instance based on the
 * provided {@link TransitionConfigurer}, which defines all possible transitions
 * between {@link RtpStatus} states triggered by {@link RtpEvent} events.
 * </p>
 */
@Component("rtpStateMachineFactory")
public class RtpStateMachineFactory implements StateMachineFactory<RtpEntity, RtpEvent> {

  private final TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> transitionConfigurer;


  /**
   * Constructs a new {@code RtpStateMachineFactory}.
   *
   * @param transitionConfigurer the configuration that defines the transitions available for the state machine;
   *                              must not be {@code null}
   */
  public RtpStateMachineFactory(
      @NonNull final TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> transitionConfigurer) {
    this.transitionConfigurer = Objects.requireNonNull(transitionConfigurer);
  }


  /**
   * Creates a new {@link RtpStateMachine} instance.
   * <p>
   * The created state machine is initialized with the transitions defined by the injected
   * {@link TransitionConfigurer}.
   * </p>
   *
   * @return a newly created {@link RtpStateMachine}
   */
  @Override
  public StateMachine<RtpEntity, RtpEvent> createStateMachine() {
    return new RtpStateMachine(this.transitionConfigurer.build());
  }
}

