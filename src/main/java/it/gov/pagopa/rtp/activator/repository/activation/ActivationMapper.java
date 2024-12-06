package it.gov.pagopa.rtp.activator.repository.activation;

import java.util.UUID;

import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.PayerID;

@Component
public class ActivationMapper {

    public Payer toDomain(ActivationEntity activationEntity) {
        PayerID payerID = new PayerID(
                UUID.fromString(activationEntity.getId()));
        return new Payer(payerID,
                activationEntity.getRtpSpId(), activationEntity.getFiscalCode(),
                activationEntity.getEffectiveActivationDate());
    }

    public ActivationEntity toDbEntity(Payer payer) {
        return ActivationEntity.builder().id(payer.payerID().getId().toString())
                .fiscalCode(payer.fiscalCode())
                .rtpSpId(payer.rtpSpId())
                .effectiveActivationDate(payer.effectiveActivationDate())
                .build();
    }
}
