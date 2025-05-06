package it.gov.pagopa.rtp.activator.repository.activation;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("activations")
public class ActivationEntity {
    @Id
    private UUID id;
    private String serviceProviderDebtor;
    private Instant effectiveActivationDate;
    @Indexed(unique = true)
    private String fiscalCode;
}
