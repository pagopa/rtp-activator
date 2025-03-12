package it.gov.pagopa.rtp.activator.controller.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.netty.handler.timeout.ReadTimeoutException;
import it.gov.pagopa.rtp.activator.model.generated.send.MalformedRequestErrorResponseDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

class RtpExceptionHandlerTest {

    private RtpExceptionHandler rtpExceptionHandler;

    private WebExchangeBindException exception;

    private BindingResult bindingResult;


    @BeforeEach
    void setUp() {
        rtpExceptionHandler = new RtpExceptionHandler();
        exception = mock(WebExchangeBindException.class);
        bindingResult = mock(BindingResult.class);
    }


    @Test
    void givenDecodingException_whenHandled_thenReturnsBadRequestWithErrorDetails() {
        // Given
        String specificCauseMessage = "Invalid JSON format";
        DecodingException decodingException = Mockito.mock(DecodingException.class);
        Throwable mostSpecificCause = Mockito.mock(Throwable.class);

        Mockito.when(decodingException.getMostSpecificCause()).thenReturn(mostSpecificCause);
        Mockito.when(mostSpecificCause.getLocalizedMessage()).thenReturn(specificCauseMessage);

        // When
        ResponseEntity<MalformedRequestErrorResponseDto> response = rtpExceptionHandler.handleDecodingException(decodingException);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status should be 400 Bad Request");
        MalformedRequestErrorResponseDto errorDto = response.getBody();
        assertNotNull(errorDto, "ErrorDto should not be null");
        assertEquals("Malformed request", errorDto.getError(), "Errors should match");
        assertEquals(specificCauseMessage, errorDto.getDetails(), "Error details should match the exception's message");
    }

    @Test
    void givenDecodingException_whenCauseIsNull_thenReturnsBadRequestWithFallbackMessage() {
        // Given
        DecodingException decodingException = Mockito.mock(DecodingException.class);
        Mockito.when(decodingException.getMostSpecificCause()).thenReturn(null);

        // When
        ResponseEntity<MalformedRequestErrorResponseDto> response = rtpExceptionHandler.handleDecodingException(decodingException);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status should be 400 Bad Request");
        MalformedRequestErrorResponseDto errorDto = response.getBody();
        assertNotNull(errorDto, "ErrorDto should not be null");
        assertEquals("Malformed request", errorDto.getError(), "Error code should match");
        assertEquals("Malformed request", errorDto.getDetails(), "Error description should be null for null cause");
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
        ResponseEntity<MalformedRequestErrorResponseDto> response = rtpExceptionHandler.handleWebExchangeBindException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals("NotNull.field1", response.getBody().getError());
        assertEquals("field1 must not be null", response.getBody().getDetails());
    }

    @Test
    void givenNoValidationErrors_whenHandleWebExchangeBindException_thenReturnEmptyErrorList() {
        // Arrange
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // Act
        ResponseEntity<MalformedRequestErrorResponseDto> response = rtpExceptionHandler.handleWebExchangeBindException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void givenReadTimeoutException_whenHandled_thenReturnsGatewayTimeout() {
        // Given
        ReadTimeoutException readTimeoutException = ReadTimeoutException.INSTANCE;

        // When
        ResponseEntity<Void> response = rtpExceptionHandler.handleReadTimeoutException(readTimeoutException);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.GATEWAY_TIMEOUT, response.getStatusCode(), "Status should be 504 Gateway Timeout");
        assertNull(response.getBody(), "Response body should be null");
    }

}