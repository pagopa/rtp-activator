package it.gov.pagopa.rtp.activator.repository.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class RtpMapper {

  public Rtp toDomain(RtpEntity rtpEntity) {
    return Rtp.builder()
        .noticeNumber(rtpEntity.getNoticeNumber())
        .amount(rtpEntity.getAmount())
        .description(rtpEntity.getDescription())
        .expiryDate(LocalDate.from(rtpEntity.getExpiryDate()))
        .payerId(rtpEntity.getPayerId())
        .payeeName(rtpEntity.getPayeeName())
        .payeeId(rtpEntity.getPayeeId())
        .resourceID(new ResourceID(rtpEntity.getResourceID()))
        .savingDateTime(LocalDateTime.from(rtpEntity.getSavingDateTime()))
        .rtpSpId(rtpEntity.getRtpSpId())
        .iban(rtpEntity.getIban())
        .payTrxRef(rtpEntity.getPayTrxRef())
        .flgConf(rtpEntity.getFlgConf())
        .build();
  }

  public RtpEntity toDbEntity(Rtp rtp) {
    return RtpEntity.builder()
        .noticeNumber(rtp.noticeNumber())
        .amount(rtp.amount())
        .description(rtp.description())
        .expiryDate(Instant.from(rtp.expiryDate()))
        .payerId(rtp.payerId())
        .payeeName(rtp.payeeName())
        .payeeId(rtp.payeeId())
        .resourceID(rtp.resourceID().getId())
        .savingDateTime(Instant.from(rtp.savingDateTime()))
        .rtpSpId(rtp.rtpSpId())
        .iban(rtp.iban())
        .payTrxRef(rtp.payTrxRef())
        .flgConf(rtp.flgConf())
        .build();
  }
}
