package it.gov.pagopa.rtp.activator.domain.registryfile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.With;
import org.springframework.validation.annotation.Validated;


@With
@Validated
public record ServiceProviderFullData(
    @NotBlank
    String spId,

    @NotBlank
    String spName,

    @NotBlank
    String tspId,

    @NotNull
    TechnicalServiceProvider tsp
) {}

