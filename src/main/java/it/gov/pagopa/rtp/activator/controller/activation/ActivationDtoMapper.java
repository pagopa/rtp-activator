package it.gov.pagopa.rtp.activator.controller.activation;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PayerDto;

@Component
public class ActivationDtoMapper {

    public ActivationDto toActivationDto(Payer payer) {
        return new ActivationDto().id(payer.activationID().getId())
                .payer(new PayerDto().fiscalCode(payer.fiscalCode()).rtpSpId(payer.serviceProviderDebtor()))
                .effectiveActivationDate(LocalDateTime.ofInstant(payer.effectiveActivationDate(), ZoneOffset.UTC));
    }
}
