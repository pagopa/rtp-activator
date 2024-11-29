package it.gov.pagopa.rtp.activator.repository;

import java.time.Instant;

import org.springframework.data.annotation.Id;
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
    private String id;
    private String rtpSpId;
    private Instant effectiveActivationDate;

    private String fiscalCode;
}
