package it.gov.pagopa.rtp.activator.statemachine;

import it.gov.pagopa.rtp.activator.domain.rtp.RtpEvent;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import org.springframework.validation.annotation.Validated;

@Validated
public class RtpTransitionKey extends TransitionKey<RtpStatus, RtpEvent> {

  public RtpTransitionKey(RtpStatus source, RtpEvent event) {
    super(source, event);
  }
}
