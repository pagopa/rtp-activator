package it.gov.pagopa.rtp.activator.repository.activation;

import it.gov.pagopa.rtp.activator.domain.payer.DeactivationReason;
import java.time.Instant;
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
@Document("deleted_activations")
public class DeletedActivationEntity {

  @Id
  private UUID id;

  @Field(name = "service_provider_debtor", targetType = FieldType.STRING)
  private String serviceProviderDebtor;

  @Field(name = "deactivation_date", targetType = FieldType.DATE_TIME)
  private Instant deactivationDate;

  @Field(name = "fiscal_code", targetType = FieldType.STRING)
  private String fiscalCode;

  @Field(name = "reason", targetType = FieldType.STRING)
  private DeactivationReason reason;
}
