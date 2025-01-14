package it.gov.pagopa.rtp.activator.repository.activation;

import java.time.Instant;
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
@Document("activations")
public class ActivationEntity {
  @Id private String id;
  private String rtpSpId;
  private Instant effectiveActivationDate;

  private String fiscalCode;
}
