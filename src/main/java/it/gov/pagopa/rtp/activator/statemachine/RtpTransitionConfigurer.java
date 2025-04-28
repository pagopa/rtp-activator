package it.gov.pagopa.rtp.activator.statemachine;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Default implementation of {@link TransitionConfigurer} for {@link RtpEntity} transitions.
 * <p>
 * This class allows for the programmatic registration of valid transitions between {@link RtpStatus}
 * states, triggered by {@link RtpEvent} events. It supports attaching optional pre- and post-transition
 * actions to each state change.
 * </p>
 */
public class RtpTransitionConfigurer implements TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> {

  /**
   * A no-operation consumer that does nothing.
   * Used as a default when no transition action is provided.
   */
  private static final Consumer<RtpEntity> NOOP_ACTION = rtpEntity -> {};

  private final Map<RtpTransitionKey, RtpTransition> transitionsMap = new ConcurrentHashMap<>();


  /**
   * Registers a basic transition with no actions.
   *
   * @param transitionKey the source state and event triggering the transition
   * @param toState       the target state after the transition
   * @return the current {@link TransitionConfigurer} instance for chaining
   */
  @Override
  public TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> register(
      TransitionKey<RtpStatus, RtpEvent> transitionKey, RtpStatus toState) {

    return this.register(transitionKey, toState, NOOP_ACTION);
  }


  /**
   * Registers a transition with a single post-transition action.
   *
   * @param transitionKey the source state and event triggering the transition
   * @param toState       the target state after the transition
   * @param action        the action to execute after the transition
   * @return the current {@link TransitionConfigurer} instance for chaining
   */
  @Override
  public TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> register(
      TransitionKey<RtpStatus, RtpEvent> transitionKey, RtpStatus toState,
      Consumer<RtpEntity> action) {

    return this.register(transitionKey, toState, Collections.emptyList(), Collections.singletonList(action));
  }


  /**
   * Registers a transition with pre and post-transition actions.
   *
   * @param transitionKey         the source state and event triggering the transition
   * @param toState                the target state after the transition
   * @param preTransitionAction    list of actions to perform before changing the state
   * @param postTransitionAction   list of actions to perform after changing the state
   * @return the current {@link TransitionConfigurer} instance for chaining
   */
  @Override
  public TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> register(
      TransitionKey<RtpStatus, RtpEvent> transitionKey, RtpStatus toState,
      List<Consumer<RtpEntity>> preTransitionAction, List<Consumer<RtpEntity>> postTransitionAction) {

    final var transition = new RtpTransition(
        transitionKey.getSource(), transitionKey.getEvent(), toState, preTransitionAction, postTransitionAction);

    this.transitionsMap.put((RtpTransitionKey) transitionKey, transition);

    return this;
  }


  /**
   * Builds and returns the {@link TransitionConfiguration} containing all registered transitions.
   *
   * @return a configured {@link TransitionConfiguration}
   */
  @Override
  public TransitionConfiguration<RtpEntity, RtpStatus, RtpEvent> build() {
    return new RtpTransitionConfiguration(this.transitionsMap);
  }
}

