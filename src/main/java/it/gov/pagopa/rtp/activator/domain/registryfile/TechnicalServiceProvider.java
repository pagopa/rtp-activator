package it.gov.pagopa.rtp.activator.domain.registryfile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.With;
import org.springframework.validation.annotation.Validated;


@With
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TechnicalServiceProvider(
    @NotBlank String id,
    @NotBlank String name,

    @NotBlank
    @JsonProperty("service_endpoint")
    String serviceEndpoint,

    @NotBlank
    @JsonProperty("certificate_serial_number")
    String certificateSerialNumber,

    OAuth2 oauth2,

    @JsonProperty("is_mtls_enabled")
    Boolean isMtlsEnabled
) {

  public TechnicalServiceProvider(
      String id,
      String name,
      String serviceEndpoint,
      String certificateSerialNumber,
      OAuth2 oauth2,
      Boolean isMtlsEnabled) {

    this.id = id;
    this.name = name;
    this.serviceEndpoint = serviceEndpoint;
    this.certificateSerialNumber = certificateSerialNumber;
    this.oauth2 = oauth2;
    this.isMtlsEnabled = (isMtlsEnabled != null) ? isMtlsEnabled : true;
  }
}

