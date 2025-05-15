package it.gov.pagopa.rtp.activator.domain.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RtpInvalidStateTransition extends Throwable {

  public RtpInvalidStateTransition(String from, String to) {
    super(String.format("Cannot transition RTP from %s to %s", from, to));
  }
}
