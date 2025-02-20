package it.gov.pagopa.rtp.activator.domain.registryfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ServiceProvider(
    @NotBlank
    @JsonProperty("id")
    String id,

    @NotBlank
    @JsonProperty("name")
    String name,

    @NotBlank
    @JsonProperty("tsp_id")
    String tspId
) {}
