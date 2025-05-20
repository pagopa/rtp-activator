package it.gov.pagopa.rtp.activator.repository.activation;


import java.util.Objects;
import java.util.UUID;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

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


    @NonNull
    @Override
    public Mono<Payer> findById(@NonNull final UUID id) {
      Objects.requireNonNull(id, "Id cannot be null");

      return this.activationDB.findById(id)
          .map(activationMapper::toDomain);
    }


    @Override
    public Mono<Payer> findByFiscalCode(String fiscalCode) {
        return activationDB.findByFiscalCode(fiscalCode)
                .map(activationMapper::toDomain);
    }

    @Override
    public Mono<Payer> save(Payer payer) {
        return activationDB.save(activationMapper.toDbEntity(payer)).map(activationMapper::toDomain);
    }

}
