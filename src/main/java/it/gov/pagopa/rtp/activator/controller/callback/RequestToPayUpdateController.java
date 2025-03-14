package it.gov.pagopa.rtp.activator.controller.callback;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.rtp.activator.domain.errors.IncorrectCertificate;
import it.gov.pagopa.rtp.activator.epcClient.model.AsynchronousSepaRequestToPayResponseResourceDto;
import it.gov.pagopa.rtp.activator.utils.CheckCertificate;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Controller implementation for handling the RequestToPayUpdate callback
 */
@RestController
@Slf4j
public class RequestToPayUpdateController implements RequestToPayUpdateApi {

  private final CheckCertificate checkCertificate;

  public RequestToPayUpdateController(
      CheckCertificate checkCertificate) {
    this.checkCertificate = checkCertificate;
  }

  @Override
  public Mono<ResponseEntity<Void>> handleRequestToPayUpdate(String clientCertificateSerialNumber,
      @Valid Mono<AsynchronousSepaRequestToPayResponseResourceDto> asynchronousSepaRequestToPayResponseResourceDto) {

    return asynchronousSepaRequestToPayResponseResourceDto
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Request body cannot be empty")))
        .flatMap(s -> checkCertificate.verifyRequestCertificate(s, clientCertificateSerialNumber))
        .<ResponseEntity<Void>>map(response -> ResponseEntity.ok().build())
        .onErrorReturn(IncorrectCertificate.class,
            ResponseEntity.status(403).build())
        .onErrorReturn(IllegalArgumentException.class, ResponseEntity.badRequest().build())
        .doOnError(a -> log.error("Error receiving the update callback {}", a.getMessage()));
  }

}
