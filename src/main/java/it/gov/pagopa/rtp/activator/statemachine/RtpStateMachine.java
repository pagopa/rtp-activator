package it.gov.pagopa.rtp.activator.statemachine;

import it.gov.pagopa.rtp.activator.domain.rtp.Event;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

/**
 * Reactive implementation of a State Machine for {@link RtpEntity} entities, using {@link Publisher}.
 * <p>
 * This class handles the evaluation and execution of state transitions based on triggering {@link RtpEvent}s.
 * It leverages a {@link TransitionConfiguration} to determine allowed transitions and apply associated actions
 * during state changes.
 * </p>
 * <p>
 * Main responsibilities:
 * <ul>
 *   <li>Check if a transition is possible from the current state for a given event.</li>
 *   <li>Perform the state transition, applying any pre- and post-transition actions.</li>
 *   <li>Update the entity's status after a successful transition.</li>
 * </ul>
 * </p>
 */
public class RtpStateMachine implements StateMachine<RtpEntity, RtpEvent> {

  private final TransitionConfiguration<RtpEntity, RtpStatus, RtpEvent> transitionConfiguration;


  /**
   * Constructs a {@code RtpStateMachine} with the given transition configuration.
   *
   * @param transitionConfiguration the configuration providing available transitions;
   *                                 must not be {@code null}
   */
  public RtpStateMachine(
      @NonNull final TransitionConfiguration<RtpEntity, RtpStatus, RtpEvent> transitionConfiguration) {

    this.transitionConfiguration = Objects.requireNonNull(transitionConfiguration);
  }


  /**
   * Checks asynchronously whether a given {@link RtpEvent} can trigger a valid transition
   * from the current status of the given {@link RtpEntity}.
   *
   * @param source the current entity to check the transition from; must not be {@code null}
   * @param event  the event that triggers the transition; must not be {@code null}
   * @return a {@link Publisher} emitting {@code true} if the transition is possible, {@code false} otherwise
   */
  @Override
  public Publisher<Boolean> canTransition(
      @NonNull final RtpEntity source, @NonNull final RtpEvent event) {

    Objects.requireNonNull(source, "Source cannot be null");
    Objects.requireNonNull(event, "Event cannot be null");

    return Mono.just(new RtpTransitionKey(source.getStatus(), event))
        .map(this::canTransition);
  }


  /**
   * Performs the transition from the current state based on the given {@link RtpEvent},
   * applying any pre-transition and post-transition actions defined in the configuration.
   *
   * @param source the entity on which to perform the transition; must not be {@code null}
   * @param event  the event that triggers the transition; must not be {@code null}
   * @return a {@link Publisher} emitting the updated {@link RtpEntity} after the transition
   * @throws IllegalStateException if no valid transition is defined for the current state and event
   */
  @NonNull
  @Override
  public Publisher<RtpEntity> transition(
      @NonNull final RtpEntity source, @NonNull final RtpEvent event) {

    Objects.requireNonNull(source, "Source cannot be null");
    Objects.requireNonNull(event, "Event cannot be null");

    return Mono.just(new RtpTransitionKey(source.getStatus(), event))
        .flatMap(transitionKey -> this.transitionConfiguration.getTransition(transitionKey)
            .map(Mono::just)
            .orElseGet(() -> Mono.error(new IllegalStateException(
                String.format("Cannot transition from %s to %s", source, event)))))
        .doOnNext(transition -> transition.getPreTransactionActions()
            .forEach(action -> action.accept(source)))
        .doOnNext(transition -> this.advanceStatus(
            source, transition.getDestination(), transition.getEvent()))
        .doOnNext(transition -> transition.getPostTransactionActions()
            .forEach(action -> action.accept(source)))
        .map(transition -> source);
  }


  /**
   * Internal helper method to check if a given {@link RtpTransitionKey} represents
   * a valid transition according to the configured transitions.
   *
   * @param transitionKey the key representing the current state and triggering event; must not be {@code null}
   * @return {@code true} if a transition exists for the given key, {@code false} otherwise
   */
  private boolean canTransition(
      @NonNull final RtpTransitionKey transitionKey) {

    Objects.requireNonNull(transitionKey, "Transition key cannot be null");

    return Optional.of(transitionKey)
        .flatMap(this.transitionConfiguration::getTransition)
        .isPresent();
  }


  /**
   * Advances the status of the given {@link RtpEntity} to a new status and updates its events.
   *
   * @param rtpEntity   the entity whose status is to be updated
   * @param newStatus   the new status to set for the entity
   * @param triggerEvent the event that triggered the status change
   * @throws NullPointerException if any of the arguments is {@code null}
   */
  @NonNull
  private void advanceStatus(
      @NonNull final RtpEntity rtpEntity,
      @NonNull final RtpStatus newStatus,
      @NonNull final RtpEvent triggerEvent) {

    Objects.requireNonNull(rtpEntity, "Entity cannot be null");
    Objects.requireNonNull(newStatus, "Status cannot be null");
    Objects.requireNonNull(triggerEvent, "Event cannot be null");

    final var updatedEvents = Stream.concat(
            rtpEntity.getEvents().stream(), Stream.of(
                Event.builder()
                    .timestamp(Instant.now())
                    .precStatus(rtpEntity.getStatus())
                    .triggerEvent(triggerEvent)
                    .build()
            ))
        .toList();

    rtpEntity.setStatus(newStatus);
    rtpEntity.setEvents(updatedEvents);
  }
}

