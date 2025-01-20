package it.gov.pagopa.rtp.activator.controller.rtp;

import it.gov.pagopa.rtp.activator.domain.errors.MessageBadFormed;
import it.gov.pagopa.rtp.activator.model.generated.send.MalformedRequestErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice(basePackages = "it.gov.pagopa.rtp.activator.controller.rtp")
public class RtpExceptionHandler {

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
}
