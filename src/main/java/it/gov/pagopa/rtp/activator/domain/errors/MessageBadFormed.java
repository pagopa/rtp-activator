package it.gov.pagopa.rtp.activator.domain.errors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorsDto;
import lombok.Getter;

@Getter
public class MessageBadFormed extends RuntimeException {

  private final transient ErrorsDto errorsDto;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public MessageBadFormed(ErrorsDto errorsDto) {
    super("Message is bad formed");
    this.errorsDto = errorsDto;
  }

  public MessageBadFormed(String jsonValue) {
    super(jsonValue);
    try {
      this.errorsDto = objectMapper.readValue(jsonValue, ErrorsDto.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
