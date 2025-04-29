package it.gov.pagopa.rtp.activator.statemachine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RtpTransitionConfigurerTest {

  private RtpTransitionConfigurer configurer;

  @BeforeEach
  void setUp() {
    configurer = new RtpTransitionConfigurer();
  }

  @Test
  void givenTransitionKeyAndTargetState_whenRegister_thenStoreTransition() {
    final var key = new RtpTransitionKey(RtpStatus.CREATED, RtpEvent.SEND_RTP);
    final var toState = RtpStatus.SENT;

    configurer.register(key, toState);
    final var result = configurer.build().getTransition(key);

    assertTrue(result.isPresent());
    assertEquals(toState, result.get().getDestination());
  }

  @Test
  void givenTransitionKeyStateAndAction_whenRegister_thenStoreTransitionWithPostAction() {
    final var key = new RtpTransitionKey(RtpStatus.CREATED, RtpEvent.SEND_RTP);
    final var toState = RtpStatus.SENT;
    final var action = mock(Consumer.class);

    configurer.register(key, toState, action);
    final var result = configurer.build().getTransition(key);

    assertTrue(result.isPresent());
    assertEquals(toState, result.get().getDestination());
    assertEquals(1, result.get().getPostTransactionActions().size());
    assertTrue(result.get().getPostTransactionActions().contains(action));
  }

  @Test
  void givenAllParameters_whenRegister_thenStoreTransitionWithAllActions() {
    final var key = new RtpTransitionKey(RtpStatus.CREATED, RtpEvent.SEND_RTP);
    final var toState = RtpStatus.SENT;
    final var preAction = mock(Consumer.class);
    final var postAction = mock(Consumer.class);

    configurer.register(key, toState, List.of(preAction), List.of(postAction));
    final var result = configurer.build().getTransition(key);

    assertTrue(result.isPresent());
    assertEquals(toState, result.get().getDestination());
    assertEquals(List.of(preAction), result.get().getPreTransactionActions());
    assertEquals(List.of(postAction), result.get().getPostTransactionActions());
  }

  @Test
  void givenNoRegistrations_whenBuild_thenReturnEmptyConfiguration() {
    final var key = new RtpTransitionKey(RtpStatus.SENT, RtpEvent.ACCEPT_RTP);
    final var transition = configurer.build().getTransition(key);
    assertTrue(transition.isEmpty());
  }

  @Test
  void givenTransitionWithActions_whenExecuted_thenActionsAreInvoked() {
    final var key = new RtpTransitionKey(RtpStatus.CREATED, RtpEvent.SEND_RTP);
    final var toState = RtpStatus.SENT;

    final var preAction = mock(Consumer.class);
    final var postAction = mock(Consumer.class);
    final var rtpEntity = new RtpEntity();

    configurer.register(key, toState, List.of(preAction), List.of(postAction));
    final var transitionOpt = configurer.build().getTransition(key);

    assertTrue(transitionOpt.isPresent());
    final var transition = transitionOpt.get();

    // Simulate transition execution
    transition.getPreTransactionActions().forEach(action -> action.accept(rtpEntity));
    transition.getPostTransactionActions().forEach(action -> action.accept(rtpEntity));

    verify(preAction).accept(rtpEntity);
    verify(postAction).accept(rtpEntity);
  }

}
