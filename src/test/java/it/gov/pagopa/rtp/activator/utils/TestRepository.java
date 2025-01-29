package it.gov.pagopa.rtp.activator.utils;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import it.gov.pagopa.rtp.activator.telemetry.TraceMongo;
import reactor.core.publisher.Mono;

public interface TestRepository extends ReactiveMongoRepository<Object, String> {
    @TraceMongo
    Mono<Object> findById(String id);

    Mono<Object> getMongoTemplate();
}