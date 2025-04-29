package it.gov.pagopa.rtp.activator.statemachine;

import java.util.List;
import java.util.function.Consumer;

public interface TransitionConfigurer<T, S, E> {

  TransitionConfigurer<T, S, E> register(TransitionKey<S, E> transitionKey, S toState);
  TransitionConfigurer<T, S, E> register(TransitionKey<S, E> transitionKey, S toState, Consumer<T> action);
  TransitionConfigurer<T, S, E> register(
      TransitionKey<S, E> transitionKey,
      S toState,
      List<Consumer<T>> preTransitionAction,
      List<Consumer<T>> postTransitionAction);

  TransitionConfiguration<T, S, E> build();

}
