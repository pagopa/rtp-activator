package it.gov.pagopa.rtp.activator.service.activation;

import it.gov.pagopa.rtp.activator.domain.payer.DeactivationReason;
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

@Service
@Slf4j
public class ActivationPayerServiceImpl implements ActivationPayerService {

    private final ActivationDBRepository activationDBRepository;

    public ActivationPayerServiceImpl(ActivationDBRepository activationDBRepository) {
        this.activationDBRepository = activationDBRepository;
    }

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


    @NonNull
    @Override
    public Mono<Payer> findPayerById(@NonNull final UUID id) {

        Objects.requireNonNull(id, "Id cannot be null");

        return this.activationDBRepository.findById(id);
    }


    @Override
    public Mono<Payer> findPayer(String payer) {
            return activationDBRepository.findByFiscalCode(payer);
    }


    @NonNull
    @Override
    public Mono<Void> deactivatePayer(@NonNull final Payer payerToDeactivate) {
        Objects.requireNonNull(payerToDeactivate, "Payer cannot be null");

        return Mono.just(payerToDeactivate)
            .doOnNext(payer -> log.info("Deactivating payer with id {} and fiscal code {}",
                payer.activationID().getId(), payer.fiscalCode()))
            .flatMap(payer ->
                this.activationDBRepository.deactivate(payer, DeactivationReason.DELETE))
            .doOnSuccess(id -> log.info("Payer deactivated with id {} and fiscal code {}",
                payerToDeactivate.activationID().getId(), payerToDeactivate.fiscalCode()))
            .doOnError(error -> log.error("Error deactivating payer: {}", error.getMessage(), error));
    }
}