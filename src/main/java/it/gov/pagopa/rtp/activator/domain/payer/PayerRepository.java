package it.gov.pagopa.rtp.activator.domain.payer;


import io.opentelemetry.instrumentation.annotations.WithSpan;
import reactor.core.publisher.Mono;

public interface PayerRepository {
    
    // Used to check if a specific payer is already registered.
    @WithSpan
    Mono<Payer> findByFiscalCode(String fiscalCode);
    
    @WithSpan
    Mono<Payer> save(Payer payer);
    
}