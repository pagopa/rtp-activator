package it.gov.pagopa.rtp.activator.service.activation;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import reactor.core.publisher.Mono;

public interface ActivationPayerService {
   Mono<Payer> activatePayer(String payer, String fiscalCode);
   Mono<Payer> findPayer(String payer);
}