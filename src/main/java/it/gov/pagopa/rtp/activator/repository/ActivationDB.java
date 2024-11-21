package it.gov.pagopa.rtp.activator.repository;

import java.util.Optional;

import com.azure.spring.data.cosmos.repository.CosmosRepository;

public interface ActivationDB extends CosmosRepository<ActivationEntity, String> {
    Optional<ActivationEntity> findByFiscalCode(String fiscalCode);

}
