package it.gov.pagopa.rtp.activator.domain.rtp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record Rtp(String noticeNumber, BigDecimal amount, String description, LocalDate expiryDate,
    String payerId, String payerName, String payeeName, String payeeId, ResourceID resourceID,
    String subject, LocalDateTime savingDateTime, String rtpSpId, String endToEndId, String iban,
    String protocolId, String flgConf, RtpStatus status) {

  public Rtp toRtpWithActivationInfo(String rtpSpId) {
    return Rtp.builder()
        .rtpSpId(rtpSpId)
        .endToEndId(this.endToEndId())
        .iban(this.iban())
        .protocolId(this.protocolId())
        .flgConf(this.flgConf())
        .payerName(this.payerName())
        .payerId(this.payerId())
        .payeeName(this.payeeName())
        .payeeId(this.payeeId())
        .noticeNumber(this.noticeNumber())
        .amount(this.amount())
        .description(this.description())
        .expiryDate(this.expiryDate())
        .resourceID(this.resourceID())
        .subject(this.subject())
        .savingDateTime(this.savingDateTime())
        .status(RtpStatus.CREATED)
        .build();
  }

  public Rtp toRtpSent(Rtp rtp) {
    return Rtp.builder()
        .rtpSpId(rtp.rtpSpId())
        .endToEndId(rtp.endToEndId())
        .iban(rtp.iban())
        .protocolId(rtp.protocolId())
        .flgConf(rtp.flgConf())
        .payerName(this.payerName())
        .payerId(rtp.payerId())
        .payeeName(rtp.payeeName())
        .payeeId(rtp.payeeId())
        .noticeNumber(rtp.noticeNumber())
        .amount(rtp.amount())
        .description(rtp.description())
        .expiryDate(rtp.expiryDate())
        .resourceID(rtp.resourceID())
        .subject(this.subject())
        .savingDateTime(rtp.savingDateTime())
        .status(RtpStatus.SENT)
        .build();
  }
}
