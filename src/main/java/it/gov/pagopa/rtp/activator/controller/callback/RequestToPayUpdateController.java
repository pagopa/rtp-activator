package it.gov.pagopa.rtp.activator.controller.callback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.rtp.activator.utils.LoggingUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.rtp.activator.domain.errors.IncorrectCertificate;
import it.gov.pagopa.rtp.activator.utils.CertificateChecker;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Controller implementation for handling the RequestToPayUpdate callback
 */
@RestController
@Slf4j
public class RequestToPayUpdateController implements RequestToPayUpdateApi {

  private final CertificateChecker certificateChecker;
  private final ObjectMapper objectMapper;

  public RequestToPayUpdateController(
      CertificateChecker certificateChecker,
      ObjectMapper objectMapper) {
    this.certificateChecker = certificateChecker;
    this.objectMapper = objectMapper;
  }

  @Override
  public Mono<ResponseEntity<Void>> handleRequestToPayUpdate(String clientCertificateSerialNumber,
      @Valid Mono<JsonNode> asynchronousSepaRequestToPayResponseResourceWrapper) {
          
    log.info("Received send callback request"); 

    return asynchronousSepaRequestToPayResponseResourceWrapper
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Request body cannot be empty")))
        .flatMap(s -> certificateChecker.verifyRequestCertificate(s, clientCertificateSerialNumber))
        .doOnNext(s -> LoggingUtils.logAsJson(() -> s, this.objectMapper))
        .<ResponseEntity<Void>>map(response -> ResponseEntity.ok().build())
        .onErrorReturn(IncorrectCertificate.class,
            ResponseEntity.status(403).build())
        .onErrorReturn(IllegalArgumentException.class, ResponseEntity.badRequest().build())
        .doOnError(a -> log.error("Error receiving the update callback {}", a.getMessage()));
  }

}
