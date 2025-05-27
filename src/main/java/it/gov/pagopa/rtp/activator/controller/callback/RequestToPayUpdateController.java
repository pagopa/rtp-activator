package it.gov.pagopa.rtp.activator.controller.callback;

import com.fasterxml.jackson.databind.JsonNode;
import it.gov.pagopa.rtp.activator.domain.errors.ServiceProviderNotFoundException;
import it.gov.pagopa.rtp.activator.service.callback.CallbackHandler;
import it.gov.pagopa.rtp.activator.utils.PayloadInfoExtractor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.rtp.activator.domain.errors.IncorrectCertificate;
import it.gov.pagopa.rtp.activator.utils.CertificateChecker;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/** Controller implementation for handling the RequestToPayUpdate callback */
@RestController
@Slf4j
public class RequestToPayUpdateController implements RequestToPayUpdateApi {

  private final CertificateChecker certificateChecker;
  private final CallbackHandler callbackHandler;

  public RequestToPayUpdateController(
          CertificateChecker certificateChecker,
          CallbackHandler callbackHandler) {
    this.certificateChecker = certificateChecker;
    this.callbackHandler = callbackHandler;
  }

  @Override
  public Mono<ResponseEntity<Void>> handleRequestToPayUpdate(
      String clientCertificateSerialNumber, @Valid Mono<JsonNode> requestBody) {

    log.info("Received send callback request");

    return requestBody
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Request body cannot be empty")))
        .doOnNext(body -> log.info("Received callback request"))
        .flatMap(callbackHandler::handle)
        .doOnSuccess(r -> log.info("CallbackHandler completed successfully"))
        .flatMap(s -> certificateChecker.verifyRequestCertificate(s, clientCertificateSerialNumber))
        .doOnSuccess(r -> log.info("Certificate verification successful"))
        .doOnNext(PayloadInfoExtractor::populateMdc)
        .<ResponseEntity<Void>>map(s -> ResponseEntity.ok().build())
        .doOnSuccess(resp -> log.info("Send callback processed successfully"))
        .doOnError(err -> log.error("Error receiving the update callback {}", err.getMessage()))
        .onErrorReturn(IncorrectCertificate.class, ResponseEntity.status(HttpStatus.FORBIDDEN).build())
        .onErrorReturn(ServiceProviderNotFoundException.class, ResponseEntity.badRequest().build())
        .onErrorReturn(IllegalArgumentException.class, ResponseEntity.badRequest().build())
        .doFinally(sig -> MDC.clear());
  }
}
