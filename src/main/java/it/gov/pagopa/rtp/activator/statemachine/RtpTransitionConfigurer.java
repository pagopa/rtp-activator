package it.gov.pagopa.rtp.activator.statemachine;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class RtpTransitionConfigurer implements TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> {

  private static final Consumer<RtpEntity> NOOP_ACTION = rtpEntity -> {};

  private final Map<RtpTransitionKey, RtpTransition> transitionsMap = new ConcurrentHashMap<>();


  @Override
  public TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> register(
      @NonNull final TransitionKey<RtpStatus, RtpEvent> transitionKey,
      @NonNull final RtpStatus toState) {

    return this.register(transitionKey, toState, NOOP_ACTION);
  }


  @Override
  public TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> register(
      @NonNull final TransitionKey<RtpStatus, RtpEvent> transitionKey,
      @NonNull final RtpStatus toState,
      @Nullable final Consumer<RtpEntity> action) {

    final var validatedAction = Optional.ofNullable(action)
        .orElse(NOOP_ACTION);

    return this.register(
        transitionKey, toState,
        Collections.emptyList(), Collections.singletonList(validatedAction));
  }


  @Override
  public TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> register(
      @NonNull final TransitionKey<RtpStatus, RtpEvent> transitionKey,
      @NonNull final RtpStatus toState,
      @Nullable final List<Consumer<RtpEntity>> preTransitionActions,
      @Nullable final List<Consumer<RtpEntity>> postTransitionActions) {

    Objects.requireNonNull(transitionKey, "transitionKey cannot be null.");
    Objects.requireNonNull(toState, "Destination state cannot be null.");

    final var validatedPreTransactionActions = Optional.ofNullable(preTransitionActions)
        .orElseGet(Collections::emptyList);

    final var validatedPostTransactionActions = Optional.ofNullable(postTransitionActions)
        .orElseGet(Collections::emptyList);

    final var transition = new RtpTransition(
        transitionKey.getSource(), transitionKey.getEvent(), toState, validatedPreTransactionActions, validatedPostTransactionActions);

    this.transitionsMap.put((RtpTransitionKey) transitionKey, transition);

    return this;
  }


  @Override
  public TransitionConfiguration<RtpEntity, RtpStatus, RtpEvent> build() {
    return new RtpTransitionConfiguration(this.transitionsMap);
  }
}
