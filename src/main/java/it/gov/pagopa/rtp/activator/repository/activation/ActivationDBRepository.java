package it.gov.pagopa.rtp.activator.repository.activation;


import it.gov.pagopa.rtp.activator.domain.payer.DeactivationReason;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.PayerRepository;
import reactor.core.publisher.Mono;


@Repository("activationDBRepository")
@Slf4j
public class ActivationDBRepository implements PayerRepository {

    private final ActivationDB activationDB;
    private final DeletedActivationDB deletedActivationDB;
    private final ActivationMapper activationMapper;


    public ActivationDBRepository(
        ActivationDB activationDB,
        DeletedActivationDB deletedActivationDB,
        ActivationMapper activationMapper) {

        this.activationDB = activationDB;
        this.deletedActivationDB = deletedActivationDB;
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


  @NonNull
  @Override
  public Mono<Void> deactivate(
      @NonNull final Payer payer,
      @NonNull final DeactivationReason deactivationReason) {

    Objects.requireNonNull(payer, "Payer cannot be null");
    Objects.requireNonNull(deactivationReason, "Deactivation reason cannot be null");

    return Mono.just(payer)
        .doFirst(() -> log.debug("Deactivating payer: {}", payer))
        .doOnNext(payerToDeactivate -> log.debug("Mapping payer to deleted entity."))
        .map(payerToDeactivate -> this.activationMapper.toDeletedDbEntity(payerToDeactivate, deactivationReason))

        .doOnNext(deletedActivationEntity -> log.debug("Saving deleted entity: {}", deletedActivationEntity))
        .flatMap(this.deletedActivationDB::save)

        .doOnNext(deletedActivationEntity -> log.debug("Deleting activation"))
        .map(DeletedActivationEntity::getId)
        .flatMap(this.activationDB::deleteById)

        .doOnSuccess(id -> log.debug("Deleted activation with id: {}", payer.activationID().getId()))
        .doOnError(error -> log.error("Error deactivating payer: {}", error.getMessage(), error));
  }
}
