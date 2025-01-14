package it.gov.pagopa.rtp.activator.controller.rtp;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;

@Component
public class RtpDtoMapper {
    public Rtp toRtp(CreateRtpDto createRtpDto) {

        return Rtp.builder().noticeNumber(createRtpDto.getNoticeNumber()).amount(createRtpDto.getAmount()).resourceID(ResourceID.createNew())
                .description(createRtpDto.getDescription()).expiryDate(createRtpDto.getExpiryDate())
                .savingDateTime(LocalDateTime.now())
                .payerId(createRtpDto.getPayerId()).payeeName(createRtpDto.getPayee().getName())
                .payeeId(createRtpDto.getPayee().getPayeeId()).rtpSpId("rtpSpId").endToEndId("endToEndId").iban("iban")
                .payTrxRef("payTrxRef").flgConf("flgConf").build();
    }

}
