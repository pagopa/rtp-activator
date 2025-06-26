package it.gov.pagopa.rtp.activator.repository.activation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("deleted_activations")
public class DeletedActivationEntity {

  @Id
  private UUID id;
  private String serviceProviderDebtor;
  private Instant deactivationDate;
  private String fiscalCode;
}
