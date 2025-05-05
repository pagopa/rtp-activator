package it.gov.pagopa.rtp.activator.controller.activation;

import it.gov.pagopa.rtp.activator.configuration.ActivationPropertiesConfig;
import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import it.gov.pagopa.rtp.activator.exception.ActivationErrorCode;
import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = ActivationPropertiesConfig.class)
@TestPropertySource("classpath:application.yaml")
class ActivationExceptionHandlerTest {

  private ActivationExceptionHandler handler;

  private WebExchangeBindException exception;

  private BindingResult bindingResult;

  @Autowired
  private ActivationPropertiesConfig activationPropertiesConfig;

  @BeforeEach
  void setUp() {
    handler = new ActivationExceptionHandler(activationPropertiesConfig);
    exception = mock(WebExchangeBindException.class);
    bindingResult = mock(BindingResult.class);
  }

  @Test
  void givenValidationErrors_whenHandleWebExchangeBindException_thenReturnBadRequestResponse() {
    // Arrange
    FieldError fieldError1 = new FieldError("objectName", "field1", "invalidValue1", false,
        new String[] { "NotNull.field1", "NotNull" }, null, "must not be null");
    FieldError fieldError2 = new FieldError("objectName", "field2", "invalidValue2", false,
        new String[] { "Invalid.field2", "Invalid" }, null, "must be a valid email");

    when(exception.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

    // Act
    ResponseEntity<ErrorsDto> response = handler.handleWebExchangeBindException(exception);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getErrors().size());

    assertEquals("NotNull.field1", response.getBody().getErrors().get(0).getCode());
    assertEquals("field1 must not be null", response.getBody().getErrors().get(0).getDescription());

    assertEquals("Invalid.field2", response.getBody().getErrors().get(1).getCode());
    assertEquals("field2 must be a valid email", response.getBody().getErrors().get(1).getDescription());
  }

  @Test
  void handlePayerAlreadyExists_ShouldReturnConflictWithLocationHeader() {
    UUID activationId = UUID.randomUUID();
    PayerAlreadyExists ex = mock(PayerAlreadyExists.class);
    when(ex.getMessage()).thenReturn("Payer already exists");
    when(ex.getExistingActivationId()).thenReturn(activationId);

    ResponseEntity<ErrorsDto> response = handler.handlePayerAlreadyExists(ex);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals(URI.create(activationPropertiesConfig.baseUrl() + activationId),
        response.getHeaders().getLocation());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getErrors().size());

    ErrorDto error = response.getBody().getErrors().get(0);
    assertEquals(ActivationErrorCode.DUPLICATE_PAYER_ID_ACTIVATION.getCode(), error.getCode());
    assertEquals(ActivationErrorCode.DUPLICATE_PAYER_ID_ACTIVATION.getMessage(), error.getDescription());
  }

  @Test
  void givenNoValidationErrors_whenHandleWebExchangeBindException_thenReturnEmptyErrorList() {
    when(exception.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors()).thenReturn(List.of());

    ResponseEntity<ErrorsDto> response = handler.handleWebExchangeBindException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getErrors().isEmpty());
  }

}