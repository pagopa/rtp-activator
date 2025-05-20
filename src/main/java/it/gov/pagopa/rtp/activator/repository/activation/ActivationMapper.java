package it.gov.pagopa.rtp.activator.repository.activation;

import it.gov.pagopa.rtp.activator.domain.payer.DeactivationReason;
import java.time.Instant;
import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.ActivationID;

@Component
public class ActivationMapper {

    public Payer toDomain(ActivationEntity activationEntity) {
        ActivationID activationID = new ActivationID((activationEntity.getId()));
        return new Payer(activationID,
                activationEntity.getServiceProviderDebtor(), activationEntity.getFiscalCode(),
                activationEntity.getEffectiveActivationDate());
    }

    public ActivationEntity toDbEntity(Payer payer) {
        return ActivationEntity.builder().id(payer.activationID().getId())
                .fiscalCode(payer.fiscalCode())
                .serviceProviderDebtor(payer.serviceProviderDebtor())
                .effectiveActivationDate(payer.effectiveActivationDate())
                .build();
    }


    public DeletedActivationEntity toDeletedDbEntity(Payer payer, DeactivationReason deactivationReason) {
        return DeletedActivationEntity.builder().id(payer.activationID().getId())
                .fiscalCode(payer.fiscalCode())
                .serviceProviderDebtor(payer.serviceProviderDebtor())
                .deactivationDate(Instant.now())
                .reason(deactivationReason)
                .build();
    }
}
