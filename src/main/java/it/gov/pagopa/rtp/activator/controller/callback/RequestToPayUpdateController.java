package it.gov.pagopa.rtp.activator.controller.callback;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.rtp.activator.domain.errors.IncorrectCertificate;
import it.gov.pagopa.rtp.activator.epcClient.model.AsynchronousSepaRequestToPayResponseResourceDto;
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

  public RequestToPayUpdateController(
      CertificateChecker certificateChecker) {
    this.certificateChecker = certificateChecker;
  }

  @Override
  public Mono<ResponseEntity<Void>> handleRequestToPayUpdate(String clientCertificateSerialNumber,
      @Valid Mono<AsynchronousSepaRequestToPayResponseResourceDto> asynchronousSepaRequestToPayResponseResourceDto) {

    return asynchronousSepaRequestToPayResponseResourceDto
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Request body cannot be empty")))
        .flatMap(s -> certificateChecker.verifyRequestCertificate(s, clientCertificateSerialNumber))
        .<ResponseEntity<Void>>map(response -> ResponseEntity.ok().build())
        .onErrorReturn(IncorrectCertificate.class,
            ResponseEntity.status(403).build())
        .onErrorReturn(IllegalArgumentException.class, ResponseEntity.badRequest().build())
        .doOnError(a -> log.error("Error receiving the update callback {}", a.getMessage()));
  }

}
