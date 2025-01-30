package it.gov.pagopa.rtp.activator.controller.rtp;

import it.gov.pagopa.rtp.activator.model.generated.send.MalformedRequestErrorResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class RtpExceptionHandlerTest {

    private RtpExceptionHandler rtpExceptionHandler;


    @BeforeEach
    void setUp() {
        rtpExceptionHandler = new RtpExceptionHandler();
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


}