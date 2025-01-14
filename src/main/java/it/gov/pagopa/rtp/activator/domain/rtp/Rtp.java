package it.gov.pagopa.rtp.activator.domain.rtp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record Rtp(
    String noticeNumber,
    BigDecimal amount,
    String description,
    LocalDate expiryDate,
    String payerId,
    String payeeName,
    String payeeId,
    ResourceID resourceID,
    LocalDateTime savingDateTime,
    String rtpSpId,
    String endToEndId,
    String iban,
    String payTrxRef,
    String flgConf) {}
