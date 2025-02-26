package it.gov.pagopa.rtp.activator.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;


@Validated
@ConfigurationProperties(prefix = "client.ssl")
public record SslContextProps(

    @NonNull
    String pfxFile,

    @DefaultValue("")
    String pfxPassword,

    @NotBlank
    String pfxType,

    @NotBlank
    String protocol

) {}
