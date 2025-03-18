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
    @JsonProperty("client_secret_env_var")
    String clientSecretEnvVar,

    @NotBlank
    @JsonProperty("scope")
    String scope,

    @JsonProperty("is_mtls_enabled")
    Boolean isMtlsEnabled
) {

    private static final boolean FALLBACK_MTLS_ENALED = true;


    public OAuth2(
        String tokenEndpoint,
        String method,
        String credentialsTransportMode,
        String clientId,
        String clientSecretKvUrl,
        String clientSecretEnvVar,
        String scope,
        Boolean isMtlsEnabled) {

        this.tokenEndpoint = tokenEndpoint;
        this.method = method;
        this.credentialsTransportMode = credentialsTransportMode;
        this.clientId = clientId;
        this.clientSecretKvUrl = clientSecretKvUrl;
        this.clientSecretEnvVar = clientSecretEnvVar;
        this.scope = scope;
        this.isMtlsEnabled = (isMtlsEnabled != null) ? isMtlsEnabled : FALLBACK_MTLS_ENALED;
    }
}

