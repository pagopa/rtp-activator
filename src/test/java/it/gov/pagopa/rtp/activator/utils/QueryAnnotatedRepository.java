package it.gov.pagopa.rtp.activator.utils;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

public interface QueryAnnotatedRepository extends ReactiveMongoRepository<Object, String> {
    @Query("customQuery")
    Mono<Object> findByCustomQuery();
}