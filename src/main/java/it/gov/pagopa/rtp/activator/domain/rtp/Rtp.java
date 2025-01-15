package it.gov.pagopa.rtp.activator.domain.rtp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record Rtp(String noticeNumber, BigDecimal amount, String description, LocalDate expiryDate,
                  String payerId, String payeeName, String payeeId, ResourceID resourceID,
                  LocalDateTime savingDateTime, String rtpSpId, String endToEndId, String iban,
                  String payTrxRef, String flgConf, RtpStatus status) {

  public Rtp toRtpWithActivationInfo(String rtpSpId) {
    return Rtp.builder()
        .rtpSpId(rtpSpId)
        .endToEndId(this.endToEndId())
        .iban(this.iban())
        .payTrxRef(this.payTrxRef())
        .flgConf(this.flgConf())
        .payerId(this.payerId())
        .payeeName(this.payeeName())
        .payeeId(this.payeeId())
        .noticeNumber(this.noticeNumber())
        .amount(this.amount())
        .description(this.description())
        .expiryDate(this.expiryDate())
        .resourceID(this.resourceID())
        .savingDateTime(this.savingDateTime())
        .status(RtpStatus.CREATED)
        .build();
  }
}
