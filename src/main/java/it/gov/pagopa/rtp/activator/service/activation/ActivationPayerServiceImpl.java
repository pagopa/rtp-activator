package it.gov.pagopa.rtp.activator.service.activation;

import java.time.Instant;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.ActivationID;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationDBRepository;
import reactor.core.publisher.Mono;


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
            .doOnSuccess(newPayer -> MDC.put("debtor", fiscalCode))
            .doOnSuccess(newPayer -> log.info("Payer activated with id: {}", newPayer.activationID().getId()))
            .doFinally(f -> MDC.clear());
    }


    /**
     * Retrieves a payer by their activation ID.
     *
     * @param id the activation ID of the payer
     * @return a {@link Mono} emitting the {@link Payer} if found, or empty if not
     */
    @NonNull
    @Override
    public Mono<Payer> findPayerById(@NonNull final UUID id) {
        Objects.requireNonNull(id, "Id cannot be null");

        return this.activationDBRepository.findById(id);
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
            .flatMap(payer ->
                this.activationDBRepository.deactivate(payer))
            .thenReturn(payerToDeactivate)
            .doOnSuccess(id -> log.info("Payer deactivated with id {}", payerToDeactivate.activationID().getId()))
            .doOnError(error -> log.error("Error deactivating payer: {}", error.getMessage(), error));
    }
}
