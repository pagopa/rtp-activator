package it.gov.pagopa.rtp.activator.statemachine;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.lang.NonNull;


/**
 * Default implementation of {@link TransitionConfiguration} for {@link RtpEntity} state transitions.
 * <p>
 * This class holds a map of possible transitions, allowing lookup of valid transitions
 * based on a given {@link RtpStatus} and {@link RtpEvent} pair.
 * </p>
 */
public class RtpTransitionConfiguration implements TransitionConfiguration<RtpEntity, RtpStatus, RtpEvent> {

  private final Map<RtpTransitionKey, RtpTransition> transitionsMap;


  /**
   * Constructs a new {@code RtpTransitionConfiguration} with the provided transitions.
   *
   * @param transitionsMap a map of all valid transitions, where each key represents a source state and event;
   *                       must not be {@code null}
   */
  public RtpTransitionConfiguration(
      @NonNull final Map<RtpTransitionKey, RtpTransition> transitionsMap) {
    this.transitionsMap = Objects.requireNonNull(transitionsMap);
  }


  /**
   * Retrieves the transition corresponding to the provided transition key.
   *
   * @param transitionKey the source state and triggering event used to lookup the transition;
   *                      must not be {@code null}
   * @return an {@link Optional} containing the matching {@link Transition}, or {@link Optional#empty()} if none found
   */
  @NonNull
  @Override
  public Optional<Transition<RtpEntity, RtpStatus, RtpEvent>> getTransition(
      @NonNull final TransitionKey<RtpStatus, RtpEvent> transitionKey) {

    Objects.requireNonNull(transitionKey, "Transition key cannot be null");

    return Optional.of(transitionKey)
        .map(RtpTransitionKey.class::cast)
        .map(this.transitionsMap::get);
  }
}

