package it.gov.pagopa.rtp.activator.statemachine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RtpStateMachineFactoryTest {

  @Mock
  private TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> transitionConfigurer;

  @Mock
  private TransitionConfiguration<RtpEntity, RtpStatus, RtpEvent> transitionConfiguration;

  private RtpStateMachineFactory factory;

  @BeforeEach
  void setUp() {
    lenient().when(transitionConfigurer.build())
        .thenReturn(transitionConfiguration);
    factory = new RtpStateMachineFactory(transitionConfigurer);
  }

  @Test
  void givenTransitionConfigurer_whenCreateStateMachine_thenReturnStateMachine() {
    final var stateMachine = factory.createStateMachine();

    assertNotNull(stateMachine);
    assertInstanceOf(RtpStateMachine.class, stateMachine);

    //Verify that the state machine is configured with the transition config
    final var entity = new RtpEntity();
    entity.setStatus(RtpStatus.CREATED);

    when(transitionConfiguration.getTransition(any()))
        .thenReturn(Optional.empty());

    StepVerifier.create(stateMachine.canTransition(entity, RtpEvent.SEND_RTP))
        .expectNext(false)
        .verifyComplete();
  }

  @Test
  void givenNullConfigurer_whenCreatingFactory_thenThrowException() {
    assertThrows(NullPointerException.class, () -> new RtpStateMachineFactory(null));
  }
}
