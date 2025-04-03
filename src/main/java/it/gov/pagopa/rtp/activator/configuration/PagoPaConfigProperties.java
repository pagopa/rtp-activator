package it.gov.pagopa.rtp.activator.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Validated
@ConfigurationProperties(prefix = "pagopa")
public record PagoPaConfigProperties(
    @NotNull Details details
) {

    public record Details(
        @NotBlank String iban,
        @NotBlank String fiscalCode) {}
}
