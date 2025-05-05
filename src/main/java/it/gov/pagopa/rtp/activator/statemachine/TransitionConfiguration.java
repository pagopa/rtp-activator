package it.gov.pagopa.rtp.activator.statemachine;

import java.util.Optional;

/**
 * Defines the contract for providing transition details in a state machine.
 * <p>
 * Implementations of this interface are responsible for returning the {@link Transition}
 * associated with a given {@link TransitionKey}, if one exists.
 * </p>
 *
 * @param <T> the type of the domain entity whose state is managed
 * @param <S> the type representing the possible states
 * @param <E> the type representing the events that trigger state transitions
 */
public interface TransitionConfiguration<T, S, E> {

  /**
   * Retrieves the {@link Transition} associated with the given {@link TransitionKey}.
   * <p>
   * If no transition exists for the provided key, an empty {@link Optional} should be returned.
   * </p>
   *
   * @param transitionKey the key identifying the source state and triggering event
   * @return an {@link Optional} containing the matching {@link Transition}, or empty if not found
   */
  Optional<Transition<T, S, E>> getTransition(TransitionKey<S, E> transitionKey);

}

