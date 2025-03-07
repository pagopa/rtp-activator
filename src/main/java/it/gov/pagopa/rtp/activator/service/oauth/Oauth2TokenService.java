package it.gov.pagopa.rtp.activator.service.oauth;

import reactor.core.publisher.Mono;

public interface Oauth2TokenService {
    public Mono<String> getAccessToken(String tokenUri, String clientId, String clientSecret, String scope);
}
