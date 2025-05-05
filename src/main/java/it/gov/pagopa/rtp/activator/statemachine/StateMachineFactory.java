package it.gov.pagopa.rtp.activator.statemachine;

/**
 * A generic factory interface for creating instances of a {@link StateMachine}.
 * <p>
 * Implementations of this interface are responsible for assembling and returning
 * fully configured instances of a state machine.
 * </p>
 *
 * @param <T> the type of the domain object whose state is managed
 * @param <E> the type representing events that trigger state transitions
 */
public interface StateMachineFactory<T, E> {

  /**
   * Creates a new {@link StateMachine} instance.
   * <p>
   * Each call to this method should return a state machine ready to process transitions
   * based on its preconfigured set of states, events, and transition rules.
   * </p>
   *
   * @return a newly created and initialized {@link StateMachine} instance
   */
  StateMachine<T, E> createStateMachine();

}

