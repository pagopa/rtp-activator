package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.registryfile.OAuth2;
import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProviderFullData;
import it.gov.pagopa.rtp.activator.domain.registryfile.TechnicalServiceProvider;
import it.gov.pagopa.rtp.activator.service.oauth.Oauth2TokenService;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * Handles OAuth2 authentication for service provider requests.
 * This class retrieves and injects access tokens into requests requiring OAuth2 authentication.
 */
@Component("oauth2Handler")
@Slf4j
public class Oauth2Handler implements RequestHandler<EpcRequest> {

  private static final String CLIENT_SECRET_ENV_VAR_PATTERN = "client.%s";

  private final Oauth2TokenService oauth2TokenService;
  private final Environment environment;

  /**
   * Constructs an instance of Oauth2Handler.
   *
   * @param oauth2TokenService the service responsible for retrieving OAuth2 tokens
   * @param environment        the application environment for retrieving configuration properties
   */
  public Oauth2Handler(
      @NonNull final Oauth2TokenService oauth2TokenService,
      @NonNull final Environment environment) {
    this.oauth2TokenService = Objects.requireNonNull(oauth2TokenService);
    this.environment = Objects.requireNonNull(environment);
  }

  /**
   * Handles OAuth2 token retrieval and injection into the request.
   * If the request requires OAuth2 authentication, it retrieves the access token
   * and adds it to the request.
   *
   * @param request the incoming request requiring authentication handling
   * @return a Mono containing the request with the access token injected if required
   */
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

  /**
   * Calls the OAuth2 token service to retrieve an access token.
   *
   * @param request the request requiring an access token
   * @return a Mono containing the request with the access token injected
   * @throws IllegalStateException if the client secret environment variable is not found
   */
  @NonNull
  private Mono<EpcRequest> callOauth2TokenService(@NonNull final EpcRequest request) {
    final var oauthData = request.serviceProviderFullData().tsp().oauth2();
    final var clientSecret = Optional.of(oauthData)
        .map(OAuth2::clientSecretEnvVar)
        .map(envVar -> this.environment.getProperty(String.format(CLIENT_SECRET_ENV_VAR_PATTERN, envVar)))
        .orElseThrow(() -> new IllegalStateException("Couldn't find client secret env var"));

    final var isMtlsEnabled = checkMtlsEnabled(request);

    return this.oauth2TokenService.getAccessToken(
            oauthData.tokenEndpoint(),
            oauthData.clientId(),
            clientSecret,
            oauthData.scope(),
            isMtlsEnabled)
        .map(request::withToken);
  }


  /**
   * Determines whether mutual TLS (mTLS) should be used for sending the RTP request.
   * It retrieves the configuration from the {@link TechnicalServiceProvider} associated
   * with the given request and checks the `mtlsEnabled` flag. If the flag is absent,
   * it defaults to {@code true}, ensuring secure communication by default.
   *
   * @param request The EPC request containing service provider details.
   * @return {@code true} if mTLS should be used, {@code false} otherwise.
   */
  @NonNull
  private boolean checkMtlsEnabled(@NonNull final EpcRequest request) {
    return Optional.of(request)
        .map(EpcRequest::serviceProviderFullData)
        .map(ServiceProviderFullData::tsp)
        .map(TechnicalServiceProvider::oauth2)
        .map(OAuth2::mtlsEnabled)
        .orElse(true);
  }

}

