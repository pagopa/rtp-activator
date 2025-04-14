package it.gov.pagopa.rtp.activator.controller.activation;

import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorsDto;
import jakarta.validation.ConstraintViolationException;
import reactor.core.publisher.Mono;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * Global exception handler for request validation errors in the activation controller.
 * <p>
 * This class provides centralized handling for validation-related exceptions occurring in
 * controllers under the package {@code it.gov.pagopa.rtp.activator.controller.activation}.
 * </p>
 *
 * <p><strong>Handled Exceptions:</strong></p>
 * <ul>
 *   <li>{@link ConstraintViolationException} - Occurs when method-level constraints are violated.</li>
 *   <li>{@link WebExchangeBindException} - Occurs when request body validation fails.</li>
 * </ul>
 *
 * <p>For each exception, an {@link ErrorsDto} object is returned, encapsulating a list of
 * {@link ErrorDto} objects describing validation errors.</p>
 */
@RestControllerAdvice(basePackages = "it.gov.pagopa.rtp.activator.controller.activation")
public class ActivationExceptionHandler {

  /**
   * Handles {@link ConstraintViolationException}, which occurs when method-level validation
   * constraints fail.
   * <p>
   * This method extracts constraint violations, converts them into a list of {@link ErrorDto}
   * objects, and returns an {@link ErrorsDto} with HTTP status {@code 400 Bad Request}.
   * </p>
   *
   * @param ex the {@link ConstraintViolationException} containing the validation errors.
   * @return a {@link ResponseEntity} with {@code 400 Bad Request} status and an {@link ErrorsDto}
   * listing the validation errors.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorsDto> handleConstraintViolation(ConstraintViolationException ex) {
    var errors = ex.getConstraintViolations().stream()
        .map(cv -> new ErrorDto()
            .code(cv.getMessageTemplate())
            .description(cv.getInvalidValue() + " " + cv.getMessage()))
        .toList();

    return handleBadRequest(errors);
  }


  /**
   * Handles {@link WebExchangeBindException}, which occurs when request body validation fails.
   * <p>
   * This method extracts field errors from the binding result, converts them into {@link ErrorDto}
   * objects, and returns an {@link ErrorsDto} with HTTP status {@code 400 Bad Request}.
   * </p>
   *
   * @param ex the {@link WebExchangeBindException} containing the validation errors.
   * @return a {@link ResponseEntity} with {@code 400 Bad Request} status and an {@link ErrorsDto}
   * listing the validation errors.
   */
  @ExceptionHandler(WebExchangeBindException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @NonNull
  public ResponseEntity<ErrorsDto> handleWebExchangeBindException(
      @NonNull final WebExchangeBindException ex) {
    final var errors = Optional.of(ex)
        .map(WebExchangeBindException::getBindingResult)
        .map(Errors::getFieldErrors)
        .stream()
        .flatMap(List::stream)
        .map(error -> {
          final var errorCode = Optional.of(error)
              .map(FieldError::getCodes)
              .filter(ArrayUtils::isNotEmpty)
              .map(codes -> codes[0])
              .orElse("");

          final var description = error.getField() + " " + error.getDefaultMessage();

          return new ErrorDto()
              .code(errorCode)
              .description(description);
        })
        .toList();

    return handleBadRequest(errors);
  }

/**
 * Handles {@link PayerAlreadyExists} exception, which occurs when attempting to create
 * an activation for a payer that already exists.
 *
 * @param ex the {@link PayerAlreadyExists} exception
 * @return a {@link ResponseEntity} with {@code 409 Conflict} status,
 *         a Location header, and an {@link ErrorsDto} describing the conflict
 */
@ExceptionHandler(PayerAlreadyExists.class)
@ResponseStatus(HttpStatus.CONFLICT)
public Mono<ResponseEntity<ErrorsDto>> handlePayerAlreadyExists(PayerAlreadyExists ex) {
    // Create the error object
    ErrorDto error = new ErrorDto()
        .code("01000000F")  // Use appropriate error code
        .description(ex.getMessage());
    
    // Create errors container with a single error
    ErrorsDto errors = new ErrorsDto();
    errors.setErrors(Collections.singletonList(error));
    
    // If we have the activation ID, use it for the location header
    if (ex.getExistingActivationId() != null) {
        String locationUrl = ex.getUrl() + 
                "/activations/" + ex.getExistingActivationId().toString();
        
        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT)
                .location(URI.create(locationUrl))
                .body(errors));
    } else {
        // If we don't have the ID (using the default constructor), just return the error body
        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errors));
    }
}

  /**
   * Constructs a standardized {@link ErrorsDto} response for bad requests.
   * <p>
   * This utility method is used to generate a consistent error response format for validation
   * exceptions.
   * </p>
   *
   * @param errorsList the list of {@link ErrorDto} objects representing validation errors.
   * @return a {@link ResponseEntity} with {@code 400 Bad Request} status and an {@link ErrorsDto}
   * containing the provided validation errors.
   */
  @NonNull
  private ResponseEntity<ErrorsDto> handleBadRequest(@NonNull final List<ErrorDto> errorsList) {
    Objects.requireNonNull(errorsList, "Errors list must not be null");

    final var errorsDto = new ErrorsDto();
    errorsDto.setErrors(errorsList);

    return ResponseEntity.badRequest().body(errorsDto);
  }
}

