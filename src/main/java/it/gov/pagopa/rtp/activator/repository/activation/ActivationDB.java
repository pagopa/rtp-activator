package it.gov.pagopa.rtp.activator.repository.activation;



import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface ActivationDB extends ReactiveMongoRepository<ActivationEntity, String> {
    Mono<ActivationEntity> findByFiscalCode(String fiscalCode);
    Mono<ActivationEntity> findByPayerId(String payerId);
}
