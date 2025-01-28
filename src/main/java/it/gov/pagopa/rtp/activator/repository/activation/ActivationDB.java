package it.gov.pagopa.rtp.activator.repository.activation;



import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import it.gov.pagopa.rtp.activator.telemetry.TraceMongo;
import reactor.core.publisher.Mono;

@Repository
@TraceMongo
public interface ActivationDB extends ReactiveMongoRepository<ActivationEntity, String> {
    Mono<ActivationEntity> findByFiscalCode(String fiscalCode);
}
