package it.gov.pagopa.rtp.activator.service.activation;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import it.gov.pagopa.rtp.activator.domain.payer.ActivationID;
import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationDBRepository;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationEntity;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import it.gov.pagopa.rtp.activator.domain.errors.PayerNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;


/**
 * Implementation of the {@link ActivationPayerService} that handles business logic for activating,
 * retrieving, and deactivating payers.
 */
@Service
@Slf4j
public class ActivationPayerServiceImpl implements ActivationPayerService {

    private final ActivationDBRepository activationDBRepository;


    /**
     * Constructs the service with a given repository.
     *
     * @param activationDBRepository the repository handling persistence of payer activations
     */
    public ActivationPayerServiceImpl(ActivationDBRepository activationDBRepository) {
        this.activationDBRepository = activationDBRepository;
    }


    /**
     * Activates new payer given their service provider debtor and fiscal code.
     * If the payer already exists, a {@link PayerAlreadyExists} exception is thrown.
     *
     * @param serviceProviderDebtor the identifier of the service provider
     * @param fiscalCode the fiscal code of the payer
     * @return a {@link Mono} emitting the created {@link Payer}
     */
    @WithSpan
    @Override
    public Mono<Payer> activatePayer(String serviceProviderDebtor, String fiscalCode) {
        ActivationID activationID = ActivationID.createNew();
        Payer payer = new Payer(activationID, serviceProviderDebtor, fiscalCode, Instant.now());

        return activationDBRepository.save(payer)
            .onErrorMap(DuplicateKeyException.class, ex -> new PayerAlreadyExists())
            .doOnSuccess(newPayer -> MDC.put("service_provider", serviceProviderDebtor))
            .doOnSuccess(newPayer -> log.info("Payer activated with id: {}", newPayer.activationID().getId()))
            .doFinally(f -> MDC.clear());
    }


    /**
     * Retrieves a payer by their activation ID.
     *
     * @param id activation ID of the payer (non-null)
     * @return {@link Mono} with the {@link Payer} if found, or error if not
     * @throws PayerNotFoundException if no payer is found with the given ID
     */
    @NonNull
    @Override
    public Mono<Payer> findPayerById(@NonNull final UUID id) {

        return Mono.just(id)
                .doFirst(() -> log.debug("Starting retrieval payer with id: {}", id))
                .flatMap(this.activationDBRepository::findById)
                .doOnNext(payer -> MDC.put("activationId", payer.activationID().getId().toString()))
                .doOnNext(payer -> log.debug("Payer retrieved with id: {}", payer.activationID().getId()))
                .doFinally(signalType -> MDC.clear())
                .switchIfEmpty(Mono.error(new PayerNotFoundException(id)));
    }


    /**
     * Retrieves a payer by their fiscal code.
     *
     * @param payer the fiscal code of the payer
     * @return a {@link Mono} emitting the {@link Payer} if found, or empty if not
     */
    @Override
    public Mono<Payer> findPayer(String payer) {
        return activationDBRepository.findByFiscalCode(payer);
    }


    /**
     * Deactivates a given payer and returns the original payer object if successful.
     *
     * @param payerToDeactivate the {@link Payer} to deactivate
     * @return a {@link Mono} emitting the original {@link Payer} after deactivation
     */
    @NonNull
    @Override
    public Mono<Payer> deactivatePayer(@NonNull final Payer payerToDeactivate) {
        Objects.requireNonNull(payerToDeactivate, "Payer cannot be null");

        return Mono.just(payerToDeactivate)
            .doOnNext(payer -> log.info("Deactivating payer with id {}", payer.activationID().getId()))
            .flatMap(this.activationDBRepository::deactivate)
            .thenReturn(payerToDeactivate)
            .doOnSuccess(id -> log.info("Payer deactivated with id {}", payerToDeactivate.activationID().getId()))
            .doOnError(error -> log.error("Error deactivating payer: {}", error.getMessage(), error));
    }

    @Override
    public Mono<Tuple2<List<ActivationEntity>, Long>> getActivationsByServiceProvider(String serviceProvider, int page, int size) {
        return activationDBRepository.getActivationsByServiceProvider(serviceProvider, page, size)
            .doOnSuccess(result -> log.info("Fetched {} activations (total count: {}) for serviceProviderDebtor: {}",
                result.getT1().size(), result.getT2(), serviceProvider))
            .doOnError(error -> log.error("Error fetching activations for serviceProviderDebtor: {}",
                serviceProvider, error));
    }
}
