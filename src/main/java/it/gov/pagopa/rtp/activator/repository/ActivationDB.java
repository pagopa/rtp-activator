package it.gov.pagopa.rtp.activator.repository;

import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;

import reactor.core.publisher.Mono;

public interface ActivationDB extends ReactiveCosmosRepository<ActivationEntity, String> {
    Mono<ActivationEntity> findByFiscalCode(String fiscalCode);
}
