package it.gov.pagopa.rtp.activator.statemachine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RtpStateMachineTest {

  @Mock
  private TransitionConfiguration<RtpEntity, RtpStatus, RtpEvent> transitionConfiguration;

  @Mock
  private Transition<RtpEntity, RtpStatus, RtpEvent> transition;

  private RtpStateMachine stateMachine;

  private final RtpEntity rtp = new RtpEntity();
  private final RtpEvent event = RtpEvent.SEND_RTP;
  private final RtpTransitionKey transitionKey = new RtpTransitionKey(RtpStatus.CREATED, RtpEvent.SEND_RTP);

  @BeforeEach
  void setUp() {
    rtp.setStatus(RtpStatus.CREATED);
    stateMachine = new RtpStateMachine(transitionConfiguration);
  }

  @Test
  void givenValidTransition_whenCanTransition_thenReturnTrue() {
    when(transitionConfiguration.getTransition(transitionKey))
        .thenReturn(Optional.of(transition));

    StepVerifier.create(stateMachine.canTransition(rtp, event))
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void givenInvalidTransition_whenCanTransition_thenReturnFalse() {
    when(transitionConfiguration.getTransition(transitionKey))
        .thenReturn(Optional.empty());

    StepVerifier.create(stateMachine.canTransition(rtp, event))
        .expectNext(false)
        .verifyComplete();
  }

  @Test
  void givenValidTransition_whenTransition_thenApplyAndReturnEntity() {
    when(transitionConfiguration.getTransition(transitionKey))
        .thenReturn(Optional.of(transition));

    when(transition.getPreTransactionActions()).thenReturn(List.of(
        entity -> entity.setPayeeName("pre-action")));

    when(transition.getDestination()).thenReturn(RtpStatus.SENT);

    when(transition.getPostTransactionActions()).thenReturn(List.of(
        entity -> entity.setPayeeId("post-action")));

    StepVerifier.create(stateMachine.transition(rtp, event))
        .assertNext(result -> {
          assertEquals(RtpStatus.SENT, result.getStatus());
          assertEquals("pre-action", result.getPayeeName());
          assertEquals("post-action", result.getPayeeId());
        })
        .verifyComplete();
  }

  @Test
  void givenInvalidTransition_whenTransition_thenThrowIllegalStateException() {
    when(transitionConfiguration.getTransition(transitionKey))
        .thenReturn(Optional.empty());

    StepVerifier.create(stateMachine.transition(rtp, event))
        .expectErrorSatisfies(e -> {
          assertInstanceOf(IllegalStateException.class, e);
          assertTrue(e.getMessage().contains("Cannot transition from"));
        })
        .verify();
  }

  @ParameterizedTest
  @MethodSource("provideNullInputsForCanTransitionAndTransition")
  void givenNullInputs_whenInvokingMethods_thenThrowNullPointerException(
      String description,
      BiFunction<RtpStateMachine, RtpEvent, Publisher<?>> methodInvoker
  ) {
    final var rtpEntity = new RtpEntity();
    rtpEntity.setStatus(RtpStatus.CREATED);

    assertThrows(NullPointerException.class, () -> methodInvoker.apply(stateMachine, RtpEvent.SEND_RTP));
  }

  private static Stream<Arguments> provideNullInputsForCanTransitionAndTransition() {
    return Stream.of(
        Arguments.of("canTransition(null, event)", (BiFunction<RtpStateMachine, RtpEvent, Publisher<?>>) (sm, ev) -> sm.canTransition(null, ev)),
        Arguments.of("canTransition(source, null)", (BiFunction<RtpStateMachine, RtpEvent, Publisher<?>>) (sm, ev) -> {
          final var source = new RtpEntity();
          source.setStatus(RtpStatus.CREATED);
          return sm.canTransition(source, null);
        }),
        Arguments.of("transition(null, event)", (BiFunction<RtpStateMachine, RtpEvent, Publisher<?>>) (sm, ev) -> sm.transition(null, ev)),
        Arguments.of("transition(source, null)", (BiFunction<RtpStateMachine, RtpEvent, Publisher<?>>) (sm, ev) -> {
          final var source = new RtpEntity();
          source.setStatus(RtpStatus.CREATED);
          return sm.transition(source, null);
        })
    );
  }

}
