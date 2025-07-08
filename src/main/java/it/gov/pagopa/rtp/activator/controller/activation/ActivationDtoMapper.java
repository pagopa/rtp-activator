package it.gov.pagopa.rtp.activator.controller.activation;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PageMetadataDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PageOfActivationsDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PayerDto;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationEntity;
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

    public ActivationDto toActivationDto(ActivationEntity entity) {
        return new ActivationDto()
            .id(entity.getId())
            .payer(new PayerDto()
                .fiscalCode(entity.getFiscalCode())
                .rtpSpId(entity.getServiceProviderDebtor()))
            .effectiveActivationDate(LocalDateTime.ofInstant(entity.getEffectiveActivationDate(), ZoneOffset.UTC));
    }

    public PageOfActivationsDto toPageDto(List<ActivationEntity> activationEntityList, Long totalElements, int page, int size) {
        List<ActivationDto> dtos = activationEntityList.stream()
            .map(this::toActivationDto)
            .toList();

        PageMetadataDto metadata = new PageMetadataDto();
        metadata.setTotalElements(totalElements);
        metadata.setTotalPages((long) Math.ceil((double) totalElements / size));
        metadata.setPage(page);
        metadata.setSize(size);

        PageOfActivationsDto pageDto = new PageOfActivationsDto();
        pageDto.setActivations(dtos);
        pageDto.setPage(metadata);
        return pageDto;
    }
}
