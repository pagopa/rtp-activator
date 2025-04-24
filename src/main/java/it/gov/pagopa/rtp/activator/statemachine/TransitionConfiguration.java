package it.gov.pagopa.rtp.activator.statemachine;

import java.util.Optional;

public interface TransitionConfiguration<T, S, E> {

  Optional<Transition<T, S, E>> getTransition(TransitionKey<S, E> transitionKey);

}
