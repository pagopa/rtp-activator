package it.gov.pagopa.rtp.activator.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import it.gov.pagopa.rtp.activator.domain.Payer;
import it.gov.pagopa.rtp.activator.domain.PayerID;
import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import it.gov.pagopa.rtp.activator.repository.ActivationDBRepository;
import reactor.core.publisher.Mono;

@Service
public class ActivationPayerServiceImpl implements ActivationPayerService {

    private final ActivationDBRepository activationDBRepository;

    public ActivationPayerServiceImpl(ActivationDBRepository activationDBRepository) {
        this.activationDBRepository = activationDBRepository;
    }

    @Override
    public Mono<Payer> activatePayer(String rtpSpId, String fiscalCode) {

        PayerID payerID = PayerID.createNew();
        Payer payer = new Payer(payerID, rtpSpId, fiscalCode, Instant.now());

        return activationDBRepository.findByFiscalCode(fiscalCode)

            .flatMap(existingEntity -> Mono.<Payer>error(new PayerAlreadyExists())) 
            .switchIfEmpty(Mono.defer(() -> activationDBRepository.save(payer)));
    }
}