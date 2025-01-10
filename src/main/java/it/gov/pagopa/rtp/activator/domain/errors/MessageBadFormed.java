package it.gov.pagopa.rtp.activator.domain.errors;

import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorsDto;
import lombok.Getter;

@Getter
public class MessageBadFormed extends RuntimeException {

  private final transient ErrorsDto errorsDto;

  public MessageBadFormed(ErrorsDto errorsDto) {
    super("Message is bad formed");
    this.errorsDto = errorsDto;
  }
}
