package it.gov.pagopa.rtp.activator.controller.callback;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import it.gov.pagopa.rtp.activator.epcClient.model.AsynchronousSepaRequestToPayResponseResourceDto;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

/**
 * Interface for handling the RequestToPayUpdate callback
 * This callback is called by the Payer's SRTP-SP to forward a SEPA
 * Request-To-Pay Response
 * to the Payee's SRTP Service Provider
 */
public interface RequestToPayUpdateApi {

  /**
   * Handles a SEPA Request-To-Pay Response forwarded by the Payer's SRTP-SP
   * 
   * @param requestBody The SEPA Request-To-Pay Response
   * @return A response entity with HTTP 200 if successful
   */
  @PostMapping(value = "/send")
  Mono<ResponseEntity<Void>> handleRequestToPayUpdate(
      @RequestHeader(value = "X-Client-Certificate-Serial", required = true) String clientCertificateSerialNumber,
      @Valid @RequestBody Mono<AsynchronousSepaRequestToPayResponseResourceDto> asynchronousSepaRequestToPayResponseResourceDto);
}
