package it.gov.pagopa.rtp.activator.domain.payer;


import java.util.UUID;
import reactor.core.publisher.Mono;

public interface PayerRepository {

    Mono<Payer> findById(UUID id);

    // Used to check if a specific payer is already registered.
    Mono<Payer> findByFiscalCode(String fiscalCode);
    
    Mono<Payer> save(Payer payer);
    
}