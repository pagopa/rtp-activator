package it.gov.pagopa.rtp.activator.domain.rtp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Rtp {
        private String noticeNumber;
        private BigDecimal amount;
        private String description;
        private LocalDate expiryDate;
        private String payerId;
        private String payeeName;
        private String payeeId;
        private ResourceID resourceID;
        private LocalDateTime savingDateTime;
        private String rtpSpId;
        private String endToEndId;
        private String iban;
        private String payTrxRef;
        private String flgConf;
}
