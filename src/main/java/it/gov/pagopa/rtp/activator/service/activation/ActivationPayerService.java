package it.gov.pagopa.rtp.activator.service.activation;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ActivationPayerService {
   Mono<Payer> activatePayer(String payer, String fiscalCode);
   Mono<Payer> findPayerById(UUID id);
   Mono<Payer> findPayer(String payer);
   Mono<Void> deactivatePayer(Payer payer);
}