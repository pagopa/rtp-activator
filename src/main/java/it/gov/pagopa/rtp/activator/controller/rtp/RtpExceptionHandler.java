package it.gov.pagopa.rtp.activator.controller.rtp;

import it.gov.pagopa.rtp.activator.domain.errors.MessageBadFormed;
import it.gov.pagopa.rtp.activator.model.generated.send.MalformedRequestErrorResponseDto;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Optional;

@RestControllerAdvice(basePackages = "it.gov.pagopa.rtp.activator.controller.rtp")
public class RtpExceptionHandler {

  private static final String MALFORMED_REQUEST_ERROR_CODE = "Malformed request";


  @ExceptionHandler(MessageBadFormed.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<MalformedRequestErrorResponseDto> handleBadRequestFromActivation(
      MessageBadFormed ex) {
    var error = ex.getErrorsDto().getErrors().getFirst();
    var responseBody = new MalformedRequestErrorResponseDto()
        .error(error.getCode())
        .details(error.getDescription());
    return ResponseEntity.badRequest().body(responseBody);
  }

  @ExceptionHandler(WebExchangeBindException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<MalformedRequestErrorResponseDto> handleConstraintViolation(
      WebExchangeBindException ex) {
    return ex.getBindingResult().getFieldErrors().stream()
        .map(error -> new MalformedRequestErrorResponseDto()
            .error(error.getCode())
            .details(error.getRejectedValue() + " " + error.getDefaultMessage()))
        .findFirst()
        .map(error -> ResponseEntity.badRequest().body(error))
        .orElse(ResponseEntity.badRequest().build());
  }


  /**
   * Handles {@link DecodingException} exceptions thrown during the processing of requests.
   * <p>
   * This method captures the exception, extracts the most specific cause of the decoding error,
   * and constructs an appropriate response containing error details. If the cause of the
   * exception is null, a fallback message ("Malformed request") is used.
   * </p>
   *
   * <p><strong>Behavior:</strong></p>
   * <ul>
   *   <li>Sets the HTTP status of the response to {@code 400 Bad Request}.</li>
   *   <li>Extracts the localized error message from the exception's most specific cause.
   *       If the cause is {@code null}, a default error code ("Malformed request") is used.</li>
   *   <li>Creates a {@link MalformedRequestErrorResponseDto} object that encapsulates
   *       the error details, including a generic error code ("Malformed request")
   *       and a description of the decoding issue.</li>
   *   <li>Returns a {@link ResponseEntity} containing the {@link MalformedRequestErrorResponseDto} as the response body.</li>
   * </ul>
   *
   * @param ex the {@link DecodingException} thrown during request decoding, typically due to malformed input.
   *           Must not be {@code null}.
   * @return a {@link ResponseEntity} with status {@code 400 Bad Request}, containing a {@link MalformedRequestErrorResponseDto}
   *         object that details the error.
   */
  @ExceptionHandler(DecodingException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<MalformedRequestErrorResponseDto> handleDecodingException(@NonNull final DecodingException ex) {
    final var description = Optional.of(ex)
            .map(DecodingException::getMostSpecificCause)
            .map(Throwable::getLocalizedMessage)
            .orElse(MALFORMED_REQUEST_ERROR_CODE);

    final var errorsDto = new MalformedRequestErrorResponseDto()
            .error(MALFORMED_REQUEST_ERROR_CODE)
            .details(description);

    return ResponseEntity.badRequest().body(errorsDto);
  }

}
