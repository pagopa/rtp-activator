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
  private UUID resourceID;
  private String noticeNumber;
  private BigDecimal amount;
  private String description;
  private Instant expiryDate;
  private String payerId;
  private String payeeName;
  private String payeeId;
  private Instant savingDateTime;
  private String rtpSpId;
  private String iban;
  private String payTrxRef;
  private String flgConf;
  private String status;
}
