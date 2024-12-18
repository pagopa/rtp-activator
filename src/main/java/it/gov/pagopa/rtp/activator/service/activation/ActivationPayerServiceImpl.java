package it.gov.pagopa.rtp.activator.service.activation;

import java.time.Instant;

import org.springframework.stereotype.Service;

import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.PayerID;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationDBRepository;
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

    @Override
    public Mono<Payer> findPayer(String payer) {
            return activationDBRepository.findByPayerId(payer);
    }
}