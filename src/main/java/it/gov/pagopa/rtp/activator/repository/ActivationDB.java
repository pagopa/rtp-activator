package it.gov.pagopa.rtp.activator.repository;



import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface ActivationDB extends ReactiveMongoRepository<ActivationEntity, String> {
    Mono<ActivationEntity> findByFiscalCode(String fiscalCode);
}
