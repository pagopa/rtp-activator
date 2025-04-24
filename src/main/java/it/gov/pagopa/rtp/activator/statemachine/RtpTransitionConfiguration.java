package it.gov.pagopa.rtp.activator.statemachine;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.lang.NonNull;


public class RtpTransitionConfiguration implements TransitionConfiguration<RtpEntity, RtpStatus, RtpEvent> {

  private final Map<RtpTransitionKey, RtpTransition> transitionsMap;


  public RtpTransitionConfiguration(
      @NonNull final Map<RtpTransitionKey, RtpTransition> transitionsMap) {
    this.transitionsMap = Objects.requireNonNull(transitionsMap);
  }


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
