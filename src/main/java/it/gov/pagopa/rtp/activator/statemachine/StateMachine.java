package it.gov.pagopa.rtp.activator.statemachine;

import org.reactivestreams.Publisher;

/**
 * A generic interface representing a reactive state machine.
 * <p>
 * This interface defines the contract for transitioning an entity {@code T} between states
 * based on an incoming event {@code E}.
 * Implementations are responsible for determining if a transition is allowed
 * and performing the transition while respecting any configured logic (e.g., pre/post actions).
 * </p>
 *
 * @param <T> the type of the domain object whose state is managed
 * @param <E> the type representing events that trigger state transitions
 */
public interface StateMachine<T, E> {

  /**
   * Determines whether a transition from the current state of the given {@code source} entity
   * is possible when the specified {@code event} occurs.
   *
   * @param source the current domain entity instance
   * @param event  the event triggering the transition
   * @return a {@link Publisher} emitting {@code true} if the transition is allowed, {@code false} otherwise
   */
  Publisher<Boolean> canTransition(T source, E event);

  /**
   * Attempts to transition the {@code source} entity to a new state based on the given {@code event}.
   *
   * @param source the current domain entity instance
   * @param event  the event triggering the state change
   * @return a {@link Publisher} emitting the updated entity after the transition, or an error if the transition is invalid
   */
  Publisher<T> transition(T source, E event);
}

