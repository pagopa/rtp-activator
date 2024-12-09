package it.gov.pagopa.rtp.activator.domain.rtp;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Rtp(String noticeNumber, Integer amount, String description, LocalDate expiryDate, String payerId,
        String payeeName, String payeeId,
        ResourceID resourceID, LocalDateTime savingDateTime,
        String rtpSpId, String endToEndId, String iban, String payTrxRef, String flgConf) {
}
