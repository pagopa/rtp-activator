package it.gov.pagopa.rtp.activator.statemachine;

import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.List;
import java.util.function.Consumer;

public interface TransitionConfigurer<T, S, E> {

  TransitionConfigurer<T, S, E> register(TransitionKey<S, E> transitionKey, S toState);
  TransitionConfigurer<T, S, E> register(TransitionKey<S, E> transitionKey, S toState, Consumer<RtpEntity> action);
  TransitionConfigurer<T, S, E> register(
      TransitionKey<S, E> transitionKey,
      S toState,
      List<Consumer<RtpEntity>> preTransitionAction,
      List<Consumer<RtpEntity>> postTransitionAction);

  TransitionConfiguration<T, S, E> build();

}
