package it.gov.pagopa.rtp.activator.statemachine;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.Objects;
import java.util.Optional;
import org.reactivestreams.Publisher;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

public class RtpStateMachine implements StateMachine<RtpEntity, RtpEvent> {

  private final TransitionConfiguration<RtpEntity, RtpStatus, RtpEvent> transitionConfiguration;


  public RtpStateMachine(
      @NonNull final TransitionConfiguration<RtpEntity, RtpStatus, RtpEvent> transitionConfiguration) {

    this.transitionConfiguration = Objects.requireNonNull(transitionConfiguration);
  }


  @Override
  public Publisher<Boolean> canTransition(
      @NonNull final RtpEntity source, @NonNull final RtpEvent event) {

    Objects.requireNonNull(source, "Source cannot be null");
    Objects.requireNonNull(event, "Event cannot be null");

    return Mono.just(new RtpTransitionKey(source.getStatus(), event))
        .map(this::canTransition);
  }


  @NonNull
  @Override
  public Publisher<RtpEntity> transition(
      @NonNull final RtpEntity source, @NonNull final RtpEvent event) {

    Objects.requireNonNull(source, "Source cannot be null");
    Objects.requireNonNull(event, "Event cannot be null");

    return Mono.just(new RtpTransitionKey(source.getStatus(), event))
        .filter(this::canTransition)
        .switchIfEmpty(
            Mono.error(new IllegalStateException(
                String.format("Cannot transition from %s to %s", source, event))))
        .flatMap(transitionKey -> this.transitionConfiguration.getTransition(transitionKey)
            .map(Mono::just)
            .orElseGet(() -> Mono.error(new IllegalStateException(
                String.format("Cannot transition from %s to %s", source, event)))))
        .doOnNext(transition -> transition.getPreTransactionActions()
            .forEach(action -> action.accept(source)))
        .doOnNext(transition -> source.setStatus(transition.getDestination()))
        .doOnNext(transition -> transition.getPostTransactionActions(
            ).forEach(action -> action.accept(source)))
        .map(transition -> source);
  }


  private boolean canTransition(
      @NonNull final RtpTransitionKey transitionKey) {

    Objects.requireNonNull(transitionKey, "Transition key cannot be null");

    return Optional.of(transitionKey)
        .flatMap(this.transitionConfiguration::getTransition)
        .isPresent();
  }

}
