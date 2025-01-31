package it.gov.pagopa.rtp.activator.repository.activation;

import java.util.UUID;

import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.ActivationID;

@Component
public class ActivationMapper {

    public Payer toDomain(ActivationEntity activationEntity) {
        ActivationID activationID = new ActivationID(
                UUID.fromString(activationEntity.getId()));
        return new Payer(activationID,
                activationEntity.getRtpServiceProviderId(), activationEntity.getFiscalCode(),
                activationEntity.getEffectiveActivationDate());
    }

    public ActivationEntity toDbEntity(Payer payer) {
        return ActivationEntity.builder().id(payer.activationID().getId().toString())
                .fiscalCode(payer.fiscalCode())
                .rtpServiceProviderId(payer.rtpServiceProviderId())
                .effectiveActivationDate(payer.effectiveActivationDate())
                .build();
    }
}
