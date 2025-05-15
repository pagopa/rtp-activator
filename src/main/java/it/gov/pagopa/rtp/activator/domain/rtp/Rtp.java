package it.gov.pagopa.rtp.activator.domain.rtp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.With;


@With
@Builder
public record Rtp(String noticeNumber, BigDecimal amount, String description, LocalDate expiryDate,
                  String payerId, String payerName, String payeeName, String payeeId,
                  ResourceID resourceID,
                  String subject, LocalDateTime savingDateTime, String serviceProviderDebtor,
                  String iban,
                  String payTrxRef, String flgConf, RtpStatus status,
                  String serviceProviderCreditor, List<Event> events) {

  public Rtp toRtpWithActivationInfo(String rtpSpId) {
    return Rtp.builder()
        .serviceProviderDebtor(rtpSpId)
        .iban(this.iban())
        .payTrxRef(this.payTrxRef())
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
        .serviceProviderCreditor(this.serviceProviderCreditor())
        .savingDateTime(this.savingDateTime())
        .status(RtpStatus.CREATED)
        .events(List.of(
            Event.builder()
                .timestamp(Instant.now())
                .triggerEvent(RtpEvent.CREATE_RTP)
                .build()
        ))
        .build();
  }
}
