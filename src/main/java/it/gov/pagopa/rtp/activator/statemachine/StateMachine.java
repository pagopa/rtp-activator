package it.gov.pagopa.rtp.activator.statemachine;

import org.reactivestreams.Publisher;

public interface StateMachine<T, E> {

  Publisher<Boolean> canTransition(T source, E event);

  Publisher<T> transition(T source, E event);

}
