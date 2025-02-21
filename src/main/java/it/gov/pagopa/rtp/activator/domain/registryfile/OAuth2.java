package it.gov.pagopa.rtp.activator.domain.registryfile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.With;
import org.springframework.validation.annotation.Validated;


@With
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OAuth2(

    @NotBlank
    @JsonProperty("token_endpoint")
    String tokenEndpoint,

    @NotBlank
    @JsonProperty("method")
    String method,

    @NotBlank
    @JsonProperty("credentials_transport_mode")
    String credentialsTransportMode,

    @NotBlank
    @JsonProperty("client_id")
    String clientId,

    @NotBlank
    @JsonProperty("client_secret_kv_url")
    String clientSecretKvUrl,

    @NotBlank
    @JsonProperty("scope")
    String scope
) {}

