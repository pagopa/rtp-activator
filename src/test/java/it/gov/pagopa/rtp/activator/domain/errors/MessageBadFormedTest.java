package it.gov.pagopa.rtp.activator.domain.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorsDto;
import org.junit.jupiter.api.Test;

class MessageBadFormedTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldCreateMessageBadFormedWithErrorsDto() {
    ErrorsDto errorsDto = new ErrorsDto();
    MessageBadFormed exception = new MessageBadFormed(errorsDto);

    assertEquals("Message is bad formed", exception.getMessage());
    assertEquals(errorsDto, exception.getErrorsDto());
  }

  @Test
  void shouldCreateMessageBadFormedWithJsonValue() throws JsonProcessingException {
    ErrorsDto errorsDto = new ErrorsDto();
    String jsonValue = objectMapper.writeValueAsString(errorsDto);
    MessageBadFormed exception = new MessageBadFormed(jsonValue);

    assertEquals(jsonValue, exception.getMessage());
    assertEquals(errorsDto, exception.getErrorsDto());
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenJsonIsInvalid() {
    String invalidJson = "invalid json";

    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
        () -> new MessageBadFormed(invalidJson));

    assertNotNull(thrown.getCause());
    assertInstanceOf(JsonProcessingException.class, thrown.getCause());
  }
}
