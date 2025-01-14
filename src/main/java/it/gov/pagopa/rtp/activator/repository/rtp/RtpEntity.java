package it.gov.pagopa.rtp.activator.repository.rtp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("rtps")
public class RtpEntity {

  @Id
  private String id;
  String noticeNumber;
  BigDecimal amount;
  String description;
  Instant expiryDate;
  String payerId;
  String payeeName;
  String payeeId;
  UUID resourceID;
  Instant savingDateTime;
  String rtpSpId;
  String iban;
  String payTrxRef;
  String flgConf;
}
