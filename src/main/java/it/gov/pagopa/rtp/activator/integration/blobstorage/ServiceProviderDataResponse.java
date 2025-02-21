package it.gov.pagopa.rtp.activator.integration.blobstorage;

import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProvider;
import it.gov.pagopa.rtp.activator.domain.registryfile.TechnicalServiceProvider;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.With;
import org.springframework.validation.annotation.Validated;


@With
@Validated
public record ServiceProviderDataResponse(
    @NotNull @NotEmpty List<TechnicalServiceProvider> tsps,
    @NotNull @NotEmpty List<ServiceProvider> sps
) {}