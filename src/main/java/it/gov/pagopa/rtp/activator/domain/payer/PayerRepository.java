package it.gov.pagopa.rtp.activator.domain.payer;


import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface PayerRepository {

    Mono<Payer> findById(UUID id);

    // Used to check if a specific payer is already registered.
    Mono<Payer> findByFiscalCode(String fiscalCode);
    
    Mono<Payer> save(Payer payer);

    Mono<Void> deactivate(Payer payer);

    Mono<Tuple2<List<Payer>, Long>> getActivationsByServiceProvider(String serviceProvider, int page, int size);
    
}