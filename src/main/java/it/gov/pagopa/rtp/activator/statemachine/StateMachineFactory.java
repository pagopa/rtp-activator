package it.gov.pagopa.rtp.activator.statemachine;

public interface StateMachineFactory<T, E> {

  StateMachine<T, E> createStateMachine();

}
