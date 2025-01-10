package it.gov.pagopa.rtp.activator.controller.activation;

import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorsDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "it.gov.pagopa.rtp.activator.controller.activation")
public class ActivationExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorsDto> handleConstraintViolation(ConstraintViolationException ex) {
    var errors = ex.getConstraintViolations().stream()
        .map(cv -> new ErrorDto().code(cv.getMessageTemplate()).description(cv.getMessage()))
        .toList();
    ErrorsDto errorsDto = new ErrorsDto();
    errorsDto.setErrors(errors);
    return ResponseEntity.badRequest().body(errorsDto);
  }
}
