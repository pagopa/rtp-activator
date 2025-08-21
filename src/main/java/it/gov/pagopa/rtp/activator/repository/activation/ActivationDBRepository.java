package it.gov.pagopa.rtp.activator.repository.activation;


import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.PayerRepository;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;


/**
 * Repository implementation that provides database operations for {@link Payer} entities.
 * <p>
 * This class interacts with the {@link ActivationDB} and {@link DeletedActivationDB}
 * for persisting, retrieving, and deactivating payer data.
 * </p>
 */
@Repository("activationDBRepository")
@Slf4j
public class ActivationDBRepository implements PayerRepository {

  private final ActivationDB activationDB;
  private final DeletedActivationDB deletedActivationDB;
  private final ActivationMapper activationMapper;
  private final ActivationRepositoryExtended activationRepositoryExtended;


  /**
   * Constructs a new {@code ActivationDBRepository} with the required dependencies.
   *
   * @param activationDB         the repository for active activation entities
   * @param deletedActivationDB  the repository for deleted activation entities
   * @param activationMapper     the mapper to convert between domain and entity models
   */
  public ActivationDBRepository(
      ActivationDB activationDB,
      DeletedActivationDB deletedActivationDB,
      ActivationMapper activationMapper, ActivationRepositoryExtended activationRepositoryExtended) {

    this.activationDB = activationDB;
    this.deletedActivationDB = deletedActivationDB;
    this.activationMapper = activationMapper;
    this.activationRepositoryExtended = activationRepositoryExtended;
  }


  /**
   * Finds a {@link Payer} by its unique activation ID.
   *
   * @param id the activation ID
   * @return a {@link Mono} emitting the {@link Payer} if found, or empty if not
   * @throws NullPointerException if {@code id} is {@code null}
   */
  @NonNull
  @Override
  public Mono<Payer> findById(@NonNull final UUID id) {
    Objects.requireNonNull(id, "Id cannot be null");

    return this.activationDB.findById(id)
        .map(activationMapper::toDomain);
  }


  /**
   * Finds a {@link Payer} by their fiscal code.
   *
   * @param fiscalCode the fiscal code of the payer
   * @return a {@link Mono} emitting the {@link Payer} if found, or empty if not
   */
  @Override
  public Mono<Payer> findByFiscalCode(String fiscalCode) {
    return Mono.just(fiscalCode)

        .doFirst(() -> log.debug("Retrieving payer by fiscal code"))
        .flatMap(this.activationDB::findByFiscalCode)
        .doOnNext(activationEntity -> log.debug("Payer retrieved. Id: {}", activationEntity.getId()))

        .doOnNext(activationEntity -> log.debug("Mapping activation to domain"))
        .map(this.activationMapper::toDomain)
        .doOnNext(payer -> log.debug("Mapped activation to domain"))

        .switchIfEmpty(Mono.defer(() -> {
          log.debug("No activation entity found by fiscal code");
          return Mono.empty();
        }));
  }


  /**
   * Saves a new {@link Payer} to the active activation repository.
   *
   * @param payer the payer to be saved
   * @return a {@link Mono} emitting the saved {@link Payer}
   */
  @Override
  public Mono<Payer> save(Payer payer) {
    return activationDB.save(activationMapper.toDbEntity(payer))
        .map(activationMapper::toDomain);
  }


  /**
   * Deactivates a given {@link Payer}, persisting the deletion reason and removing the active record.
   *
   * @param payer               the payer to deactivate
   * @return a {@link Mono<Void>} that completes when the operation finishes
   * @throws NullPointerException if {@code payer} is {@code null}
   */
  @NonNull
  @Override
  public Mono<Void> deactivate(@NonNull final Payer payer) {

    Objects.requireNonNull(payer, "Payer cannot be null");

    return Mono.just(payer)
        .doFirst(() -> log.debug("Deactivating payer with activationId: {}", payer.activationID().getId()))
        .doOnNext(payerToDeactivate -> log.debug("Mapping payer to deleted entity."))
        .map(this.activationMapper::toDeletedDbEntity)

        .doOnNext(deletedActivationEntity -> log.debug("Saving deleted entity with id: {}", deletedActivationEntity.getId()))
        .flatMap(this.deletedActivationDB::save)

        .doOnNext(deletedActivationEntity -> log.debug("Deleting activation"))
        .map(DeletedActivationEntity::getId)
        .flatMap(this.activationDB::deleteById)

        .doOnSuccess(id -> log.debug("Deleted activation with id: {}", payer.activationID().getId()))
        .doOnError(error -> log.error("Error deactivating payer: {}", error.getMessage(), error));
  }

  @Override
  public Mono<Tuple2<List<Payer>, Long>> getActivationsByServiceProvider(String serviceProvider, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return activationRepositoryExtended.findByServiceProviderDebtor(serviceProvider, pageable)
        .doOnNext(entity -> log.debug("Mapping ActivationEntity to Payer"))
        .map(activationMapper::toDomain)
        .collectList()
        .zipWith(activationRepositoryExtended.countByServiceProviderDebtor(serviceProvider));
  }
}

