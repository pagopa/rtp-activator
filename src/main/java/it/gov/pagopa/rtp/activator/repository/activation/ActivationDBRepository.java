package it.gov.pagopa.rtp.activator.repository.activation;


import org.springframework.stereotype.Repository;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.PayerRepository;
import reactor.core.publisher.Mono;

@Repository
public class ActivationDBRepository implements PayerRepository {

    private final ActivationDB activationDB;
    private final ActivationMapper activationMapper;

    public ActivationDBRepository(ActivationDB activationDB,
            ActivationMapper activationMapper) {
        this.activationDB = activationDB;
        this.activationMapper = activationMapper;
    }

    @Override
    @WithSpan
    public Mono<Payer> findByFiscalCode(String fiscalCode) {
        return activationDB.findByFiscalCode(fiscalCode)
                .map(activationMapper::toDomain);
    }

    @Override
    @WithSpan
    public Mono<Payer> save(Payer payer) {
        return activationDB.save(activationMapper.toDbEntity(payer)).map(activationMapper::toDomain);
    }

}
