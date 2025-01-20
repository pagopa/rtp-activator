package it.gov.pagopa.rtp.activator.repository.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class RtpMapper {

  public Rtp toDomain(RtpEntity rtpEntity) {
    return Rtp.builder()
        .noticeNumber(rtpEntity.getNoticeNumber())
        .amount(rtpEntity.getAmount())
        .description(rtpEntity.getDescription())
        .expiryDate(LocalDate.ofInstant(rtpEntity.getExpiryDate(), ZoneOffset.UTC))
        .payerId(rtpEntity.getPayerId())
        .payeeName(rtpEntity.getPayeeName())
        .payeeId(rtpEntity.getPayeeId())
        .resourceID(new ResourceID(rtpEntity.getResourceID()))
        .savingDateTime(LocalDateTime.ofInstant(rtpEntity.getSavingDateTime(), ZoneOffset.UTC))
        .rtpSpId(rtpEntity.getRtpSpId())
        .iban(rtpEntity.getIban())
        .payTrxRef(rtpEntity.getPayTrxRef())
        .flgConf(rtpEntity.getFlgConf())
        .subject(rtpEntity.getSubject())
        .status(RtpStatus.valueOf(rtpEntity.getStatus()))
        .build();
  }

  public RtpEntity toDbEntity(Rtp rtp) {
    return RtpEntity.builder()
        .noticeNumber(rtp.noticeNumber())
        .amount(rtp.amount())
        .description(rtp.description())
        .expiryDate(rtp.expiryDate().atStartOfDay().toInstant(ZoneOffset.UTC))
        .payerId(rtp.payerId())
        .payeeName(rtp.payeeName())
        .payeeId(rtp.payeeId())
        .resourceID(rtp.resourceID().getId())
        .savingDateTime(rtp.savingDateTime().toInstant(ZoneOffset.UTC))
        .rtpSpId(rtp.rtpSpId())
        .iban(rtp.iban())
        .payTrxRef(rtp.payTrxRef())
        .flgConf(rtp.flgConf())
        .subject(rtp.subject())
        .status(rtp.status().name())
        .build();
  }
}
