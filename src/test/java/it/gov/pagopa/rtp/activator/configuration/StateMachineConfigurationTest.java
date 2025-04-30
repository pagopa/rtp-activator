package it.gov.pagopa.rtp.activator.configuration;

import static org.junit.jupiter.api.Assertions.*;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpDB;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import it.gov.pagopa.rtp.activator.statemachine.RtpTransitionKey;
import it.gov.pagopa.rtp.activator.statemachine.TransitionConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StateMachineConfigurationTest {

  @Mock
  private RtpDB rtpDB;

  private StateMachineConfiguration configuration;

  @BeforeEach
  void setUp() {
    configuration = new StateMachineConfiguration(rtpDB);
  }

  @Test
  void givenStateMachineConfig_whenCreatingConfigurer_thenAllTransitionsAreRegistered() {
    final var configurer = configuration.transitionConfigurer();
    final var config = configurer.build();

    assertAll(
        () -> assertTransitionExists(config, RtpStatus.CREATED, RtpEvent.SEND_RTP, RtpStatus.SENT),
        () -> assertTransitionExists(config, RtpStatus.CREATED, RtpEvent.ERROR_SEND_RTP, RtpStatus.ERROR_SEND),
        () -> assertTransitionExists(config, RtpStatus.CREATED, RtpEvent.ACCEPT_RTP, RtpStatus.ACCEPTED),
        () -> assertTransitionExists(config, RtpStatus.CREATED, RtpEvent.REJECT_RTP, RtpStatus.REJECTED),
        () -> assertTransitionExists(config, RtpStatus.CREATED, RtpEvent.USER_ACCEPT_RTP, RtpStatus.USER_ACCEPTED),
        () -> assertTransitionExists(config, RtpStatus.CREATED, RtpEvent.USER_REJECT_RTP, RtpStatus.USER_REJECTED),
        () -> assertTransitionExists(config, RtpStatus.CREATED, RtpEvent.PAY_RTP, RtpStatus.PAYED),
        () -> assertTransitionExists(config, RtpStatus.CREATED, RtpEvent.CANCEL_RTP, RtpStatus.CANCELLED),
        () -> assertTransitionExists(config, RtpStatus.SENT, RtpEvent.ACCEPT_RTP, RtpStatus.ACCEPTED),
        () -> assertTransitionExists(config, RtpStatus.SENT, RtpEvent.REJECT_RTP, RtpStatus.REJECTED),
        () -> assertTransitionExists(config, RtpStatus.SENT, RtpEvent.USER_ACCEPT_RTP, RtpStatus.USER_ACCEPTED),
        () -> assertTransitionExists(config, RtpStatus.SENT, RtpEvent.USER_REJECT_RTP, RtpStatus.USER_REJECTED),
        () -> assertTransitionExists(config, RtpStatus.SENT, RtpEvent.PAY_RTP, RtpStatus.PAYED),
        () -> assertTransitionExists(config, RtpStatus.SENT, RtpEvent.CANCEL_RTP, RtpStatus.CANCELLED),
        () -> assertTransitionExists(config, RtpStatus.ACCEPTED, RtpEvent.USER_ACCEPT_RTP, RtpStatus.USER_ACCEPTED),
        () -> assertTransitionExists(config, RtpStatus.ACCEPTED, RtpEvent.USER_REJECT_RTP, RtpStatus.USER_REJECTED),
        () -> assertTransitionExists(config, RtpStatus.ACCEPTED, RtpEvent.CANCEL_RTP, RtpStatus.CANCELLED),
        () -> assertTransitionExists(config, RtpStatus.USER_ACCEPTED, RtpEvent.PAY_RTP, RtpStatus.PAYED),
        () -> assertTransitionExists(config, RtpStatus.USER_ACCEPTED, RtpEvent.CANCEL_RTP, RtpStatus.CANCELLED),
        () -> assertTransitionExists(config, RtpStatus.CANCELLED, RtpEvent.CANCEL_RTP_ACCR, RtpStatus.CANCELLED_ACCR),
        () -> assertTransitionExists(config, RtpStatus.CANCELLED, RtpEvent.CANCEL_RTP_REJECTED, RtpStatus.CANCELLED_REJECTED),
        () -> assertTransitionExists(config, RtpStatus.CANCELLED, RtpEvent.ERROR_CANCEL_RTP, RtpStatus.ERROR_CANCEL)
    );
  }

  private void assertTransitionExists(
      TransitionConfiguration<RtpEntity, RtpStatus, RtpEvent> config,
      RtpStatus source,
      RtpEvent event,
      RtpStatus expectedTarget
  ) {
    final var key = new RtpTransitionKey(source, event);
    final var transition = config.getTransition(key);

    assertTrue(transition.isPresent(), "Expected transition not found for: " + key);
    assertEquals(expectedTarget, transition.get().getDestination(), "Unexpected destination for: " + key);

    final var postActions = transition.get()
        .getPostTransactionActions();

    assertFalse(postActions.isEmpty(), "No post-actions found for: " + key);
  }
}
