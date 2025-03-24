package it.gov.pagopa.rtp.activator.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Validated
@ConfigurationProperties(prefix = "callback")
public record CallbackProperties(
    @NotNull UrlProperties url
) {

  public record UrlProperties(
      @NotBlank String send,
      @NotBlank String cancel) {}
}

