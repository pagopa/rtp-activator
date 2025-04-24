package it.gov.pagopa.rtp.activator.statemachine;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RtpTransitionConfigurer implements TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> {

  private static final Consumer<RtpEntity> NOOP_CONSUMER = rtpEntity -> {};

  private final Map<RtpTransitionKey, RtpTransition> transitionsMap = new ConcurrentHashMap<>();


  @Override
  public TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> register(
      TransitionKey<RtpStatus, RtpEvent> transitionKey, RtpStatus toState) {

    return this.register(transitionKey, toState, NOOP_CONSUMER);
  }


  @Override
  public TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> register(
      TransitionKey<RtpStatus, RtpEvent> transitionKey, RtpStatus toState,
      Consumer<RtpEntity> action) {

    return this.register(transitionKey, toState, Collections.emptyList(), Collections.singletonList(action));
  }


  @Override
  public TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> register(
      TransitionKey<RtpStatus, RtpEvent> transitionKey, RtpStatus toState,
      List<Consumer<RtpEntity>> preTransitionAction, List<Consumer<RtpEntity>> postTransitionAction) {

    final var transition = new RtpTransition(
        transitionKey.getSource(), transitionKey.getEvent(), toState, preTransitionAction, postTransitionAction);

    this.transitionsMap.put((RtpTransitionKey) transitionKey, transition);

    return this;
  }


  @Override
  public TransitionConfiguration<RtpEntity, RtpStatus, RtpEvent> build() {
    return new RtpTransitionConfiguration(this.transitionsMap);
  }
}
