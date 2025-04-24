package it.gov.pagopa.rtp.activator.configuration;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpDB;
import it.gov.pagopa.rtp.activator.repository.rtp.RtpEntity;
import it.gov.pagopa.rtp.activator.statemachine.RtpTransitionConfigurer;
import it.gov.pagopa.rtp.activator.statemachine.RtpTransitionKey;
import it.gov.pagopa.rtp.activator.statemachine.TransitionConfigurer;
import java.util.Objects;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;


@Configuration
public class StateMachineConfiguration {

  private final RtpDB rtpRepository;


  public StateMachineConfiguration(@NonNull final RtpDB rtpRepository) {
    this.rtpRepository = Objects.requireNonNull(rtpRepository);
  }


  @Bean("transitionConfigurer")
  public TransitionConfigurer<RtpEntity, RtpStatus, RtpEvent> transitionConfigurer() {
    return new RtpTransitionConfigurer()
        .register(new RtpTransitionKey(RtpStatus.CREATED, RtpEvent.SEND_RTP), RtpStatus.SENT, persistRtp())
        .register(new RtpTransitionKey(RtpStatus.CREATED, RtpEvent.ERROR_SEND_RTP), RtpStatus.ERROR_SEND, persistRtp())

        .register(new RtpTransitionKey(RtpStatus.SENT, RtpEvent.ACCEPT_RTP), RtpStatus.ACCEPTED, persistRtp())
        .register(new RtpTransitionKey(RtpStatus.SENT, RtpEvent.REJECT_RTP), RtpStatus.REJECTED, persistRtp())
        .register(new RtpTransitionKey(RtpStatus.SENT, RtpEvent.USER_ACCEPT_RTP), RtpStatus.USER_ACCEPTED, persistRtp())
        .register(new RtpTransitionKey(RtpStatus.SENT, RtpEvent.USER_REJECT_RTP), RtpStatus.USER_REJECTED, persistRtp())
        .register(new RtpTransitionKey(RtpStatus.SENT, RtpEvent.PAY_RTP), RtpStatus.PAYED, persistRtp())
        .register(new RtpTransitionKey(RtpStatus.SENT, RtpEvent.CANCEL_RTP), RtpStatus.CANCELLED, persistRtp())

        .register(new RtpTransitionKey(RtpStatus.ACCEPTED, RtpEvent.USER_ACCEPT_ACCEPTED_RTP), RtpStatus.USER_ACCEPTED, persistRtp())
        .register(new RtpTransitionKey(RtpStatus.ACCEPTED, RtpEvent.USER_REJECT_ACCEPTED_RTP), RtpStatus.USER_REJECTED, persistRtp())
        .register(new RtpTransitionKey(RtpStatus.ACCEPTED, RtpEvent.CANCEL_ACCEPTED_RTP), RtpStatus.CANCELLED, persistRtp())

        .register(new RtpTransitionKey(RtpStatus.USER_ACCEPTED, RtpEvent.PAY_USER_ACCEPTED_RTP), RtpStatus.PAYED, persistRtp())
        .register(new RtpTransitionKey(RtpStatus.USER_ACCEPTED, RtpEvent.CANCEL_USER_ACCEPTED_RTP), RtpStatus.CANCELLED, persistRtp())

        .register(new RtpTransitionKey(RtpStatus.CANCELLED, RtpEvent.CANCEL_RTP_ACCR), RtpStatus.CANCELLED_ACCR, persistRtp())
        .register(new RtpTransitionKey(RtpStatus.CANCELLED, RtpEvent.CANCEL_RTP_REJECTED), RtpStatus.CANCELLED_REJECTED, persistRtp())
        .register(new RtpTransitionKey(RtpStatus.CANCELLED, RtpEvent.ERROR_CANCEL_RTP), RtpStatus.ERROR_CANCEL, persistRtp());
  }


  private Consumer<RtpEntity> persistRtp() {
    return this.rtpRepository::save;
  }

}
