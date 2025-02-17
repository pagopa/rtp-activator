package it.gov.pagopa.rtp.activator.controller.activation;

import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActivationExceptionHandlerTest {

    private ActivationExceptionHandler handler;

    private WebExchangeBindException exception;

    private BindingResult bindingResult;


    @BeforeEach
    void setUp() {
        handler = new ActivationExceptionHandler();
        exception = mock(WebExchangeBindException.class);
        bindingResult = mock(BindingResult.class);
    }


    @Test
    void givenValidationErrors_whenHandleWebExchangeBindException_thenReturnBadRequestResponse() {
        // Arrange
        FieldError fieldError1 = new FieldError("objectName", "field1", "invalidValue1", false,
            new String[]{"NotNull.field1", "NotNull"}, null, "must not be null");
        FieldError fieldError2 = new FieldError("objectName", "field2", "invalidValue2", false,
            new String[]{"Invalid.field2", "Invalid"}, null, "must be a valid email");

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
    void givenNoValidationErrors_whenHandleWebExchangeBindException_thenReturnEmptyErrorList() {
        // Arrange
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // Act
        ResponseEntity<ErrorsDto> response = handler.handleWebExchangeBindException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getErrors().isEmpty());
    }

}