package it.gov.pagopa.rtp.activator.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.With;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@With
@Validated
@ConfigurationProperties(prefix = "pagopa")
public record PagoPaConfigProperties(
    @NotNull Anag anag
) {

    @With
    public record Anag(
        @NotBlank String iban,
        @NotBlank String fiscalCode) {}
}
