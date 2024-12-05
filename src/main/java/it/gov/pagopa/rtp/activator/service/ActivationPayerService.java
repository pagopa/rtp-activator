package it.gov.pagopa.rtp.activator.service;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import reactor.core.publisher.Mono;

public interface ActivationPayerService {
   Mono<Payer> activatePayer(String payer, String fiscalCode);
}