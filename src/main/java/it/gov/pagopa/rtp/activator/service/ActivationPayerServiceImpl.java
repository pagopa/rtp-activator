package it.gov.pagopa.rtp.activator.service;

import java.lang.foreign.Linker.Option;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.azure.cosmos.models.PartitionKey;

import it.gov.pagopa.rtp.activator.domain.Payer;
import it.gov.pagopa.rtp.activator.domain.PayerID;
import it.gov.pagopa.rtp.activator.model.generated.ActivationDto;
import it.gov.pagopa.rtp.activator.repository.ActivationEntity;
import it.gov.pagopa.rtp.activator.repository.ActivationDB;
import it.gov.pagopa.rtp.activator.repository.ActivationDBRepository;

@Service
public class ActivationPayerServiceImpl implements ActivationPayerService {

    private final ActivationDBRepository activationDBRepository;

    public ActivationPayerServiceImpl(ActivationDBRepository activationDBRepository) {
        this.activationDBRepository = activationDBRepository;
    }

    @Override
    public ActivationDto activatePayer(String rtpSpId, String fiscalCode) {
      
        Optional<Payer> existing_item = activationDBRepository.findByFiscalCode(fiscalCode);
        if (existing_item.isPresent()) {
            // if the record already exists
            // report 409 error
        } else {
            PayerID payerID = PayerID.createNew();
            Payer payer = new Payer(payerID, rtpSpId, fiscalCode,  new Date());
            activationDBRepository.save(payer);
        }
        throw new UnsupportedOperationException("Unimplemented method 'activatePayer'");

    }

}
