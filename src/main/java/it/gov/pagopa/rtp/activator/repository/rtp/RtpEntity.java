package it.gov.pagopa.rtp.activator.repository.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.Event;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

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
  private String payerName;
  private String payerId;
  private String payeeName;
  private String payeeId;
  private String subject;
  private Instant savingDateTime;
  private String serviceProviderDebtor;
  private String iban;
  private String payTrxRef;
  private String flgConf;
  @Field(name = "status", targetType = FieldType.STRING)
  private RtpStatus status;
  private String serviceProviderCreditor;
  private List<Event> events;

}
