package it.gov.pagopa.rtp.activator.configuration;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@RequiredArgsConstructor
@Validated
@ConfigurationProperties(prefix = "pagopa")
public class PagoPaConfigProperties {

    private Anag anag;

    @With
    public record Anag(@NotBlank String iban) {}
}
