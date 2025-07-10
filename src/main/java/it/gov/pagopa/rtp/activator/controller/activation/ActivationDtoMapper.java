package it.gov.pagopa.rtp.activator.controller.activation;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PageMetadataDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PageOfActivationsDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PayerDto;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ActivationDtoMapper {

    public ActivationDto toActivationDto(Payer payer) {
        return new ActivationDto().id(payer.activationID().getId())
                .payer(new PayerDto().fiscalCode(payer.fiscalCode()).rtpSpId(payer.serviceProviderDebtor()))
                .effectiveActivationDate(LocalDateTime.ofInstant(payer.effectiveActivationDate(), ZoneOffset.UTC));
    }

    public PageOfActivationsDto toPageDto(List<Payer> activationEntityList, Long totalElements, int page, int size){
        List<ActivationDto> activations = activationEntityList.stream()
            .map(this::toActivationDto)
            .toList();

        PageMetadataDto metadata = new PageMetadataDto();
        metadata.totalElements(totalElements);
        metadata.totalPages((long) Math.ceil((double) totalElements / size));
        metadata.page(page);
        metadata.size(size);

        return new PageOfActivationsDto()
            .activations(activations)
            .page(metadata);
    }
}
