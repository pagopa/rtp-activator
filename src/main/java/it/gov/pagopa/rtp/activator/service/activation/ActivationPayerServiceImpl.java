package it.gov.pagopa.rtp.activator.service.activation;

import java.time.Instant;

import lombok.extern.slf4j.Slf4j;
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

    @Override
    public Mono<Payer> activatePayer(String serviceProviderDebtor, String fiscalCode) {

        ActivationID activationID = ActivationID.createNew();
        Payer payer = new Payer(activationID, serviceProviderDebtor, fiscalCode, Instant.now());

        return activationDBRepository.findByFiscalCode(fiscalCode)
            .flatMap(existingEntity -> Mono.<Payer>error(new PayerAlreadyExists()))
            .switchIfEmpty(Mono.defer(() -> activationDBRepository.save(payer)))
            .doOnSuccess(newPayer -> log.info("Payer activated with id: {}", newPayer.activationID().getId()));
    }

    @Override
    public Mono<Payer> findPayer(String payer) {
            return activationDBRepository.findByFiscalCode(payer);
    }
}