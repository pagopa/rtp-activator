package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.registryfile.OAuth2;
import it.gov.pagopa.rtp.activator.service.oauth.Oauth2TokenService;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component("oauth2Handler")
@Slf4j
public class Oauth2Handler implements RequestHandler<EpcRequest> {

  private static final String CLIENT_SECRET_ENV_VAR_PATTERN = "client.%s";

  private final Oauth2TokenService oauth2TokenService;
  private final Environment environment;


  public Oauth2Handler(
      @NonNull final Oauth2TokenService oauth2TokenService,
      @NonNull final Environment environment) {

    this.oauth2TokenService = Objects.requireNonNull(oauth2TokenService);
    this.environment = Objects.requireNonNull(environment);
  }


  @NonNull
  @Override
  public Mono<EpcRequest> handle(@NonNull final EpcRequest request) {
    return Mono.just(request)
        .doOnNext(req -> log.info("Handling OAuth2 for {}", req.serviceProviderFullData().spName()))
        .filter(req -> req.serviceProviderFullData().tsp().oauth2() != null)
        .doOnNext(req -> log.info("Retrieving access token"))
        .flatMap(this::callOauth2TokenService)
        .doOnNext(req -> log.info("Successfully retrieved access token"))
        .switchIfEmpty(Mono.fromSupplier(() -> {
          log.info("Skipping OAuth2 token retrieval");
          return request;
        }));
  }


  @NonNull
  private Mono<EpcRequest> callOauth2TokenService(@NonNull final EpcRequest request) {
    final var oauthData = request.serviceProviderFullData().tsp().oauth2();
    final var clientSecret = Optional.of(oauthData)
        .map(OAuth2::clientSecretEnvVar)
        .map(envVar -> this.environment.getProperty(String.format(CLIENT_SECRET_ENV_VAR_PATTERN, envVar)))
        .orElseThrow(() -> new IllegalStateException("Couldn't find client secret env var"));

    return this.oauth2TokenService.getAccessToken(
            oauthData.tokenEndpoint(),
            oauthData.clientId(),
            clientSecret,
            oauthData.scope())
        .map(request::withToken);
  }
}
