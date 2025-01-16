package it.gov.pagopa.rtp.activator.controller.rtp;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;

@Component
public class RtpDtoMapper {
  public Rtp toRtp(CreateRtpDto createRtpDto) {

    return Rtp.builder().noticeNumber(createRtpDto.getPaymentNotice().getNoticeNumber())
        .amount(createRtpDto.getPaymentNotice().getAmount()).resourceID(ResourceID.createNew())
        .description(createRtpDto.getPaymentNotice().getDescription())
        .expiryDate(createRtpDto.getPaymentNotice().getExpiryDate())
        .savingDateTime(LocalDateTime.now())
        .payerName(createRtpDto.getPayer().getName())
        .payerId(createRtpDto.getPayer().getPayerId()).payeeName(createRtpDto.getPayee().getName())
        .payeeId(createRtpDto.getPayee().getPayeeId()).rtpSpId("rtpSpId").endToEndId("endToEndId").iban("iban")
        .subject(createRtpDto.getPaymentNotice().getSubject())
        .protocolId(createRtpDto.getPayee().getProtocolId()).flgConf("flgConf").build();
  }

}
