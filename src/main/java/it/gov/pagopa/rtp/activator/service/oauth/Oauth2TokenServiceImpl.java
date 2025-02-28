package it.gov.pagopa.rtp.activator.service.oauth;


import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import it.gov.pagopa.rtp.activator.configuration.MtlsWebClient.MtlsWebClientFactory;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class Oauth2TokenServiceImpl implements Oauth2TokenService {
    private MtlsWebClientFactory mtlsWebClientFactory;

    public Oauth2TokenServiceImpl(MtlsWebClientFactory mtlsWebClientFactory) {
        this.mtlsWebClientFactory = mtlsWebClientFactory;
    }

    @Override
    public Mono<String> getAccessToken(String tokenUri, String clientId, String clientSecret, String scope) {
        if (clientId == null || clientSecret == null || tokenUri == null) {
            return Mono.error(new IllegalStateException("OAuth2 configuration params not complete"));
        }

        WebClient webClient = mtlsWebClientFactory.createMtlsWebClient();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credential");
        if (scope != null ) {
            formData.add("scope", scope);
        }

        return webClient.post().uri(tokenUri).header("clientId", clientId).header("clientSecret", clientSecret)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(BodyInserters.fromFormData(formData))
                .retrieve().bodyToMono(Map.class).map(response -> (String) response.get("access_token"))
                .doOnError(e -> log.error("Failed to obtain OAuth2 token {}", e.getMessage()));
    }

}
