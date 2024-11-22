package it.gov.pagopa.rtp.activator.repository;

import java.time.Instant;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Container(containerName = "activations", autoCreateContainer = false)
public class ActivationEntity {
    private String id;
    private String rtpSpId;
    private Instant effectiveActivationDate;

    @PartitionKey
    private String fiscalCode;
}
