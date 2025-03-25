package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.configuration.OpenAPIClientFactory;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.configuration.mtlswebclient.WebClientFactory;
import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProviderFullData;
import it.gov.pagopa.rtp.activator.domain.registryfile.TechnicalServiceProvider;
import it.gov.pagopa.rtp.activator.epcClient.api.DefaultApi;
import it.gov.pagopa.rtp.activator.service.rtp.SepaRequestToPayMapper;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;


/**
 * Abstract base class for handling EPC API invocations.
 * <p>
 * This class provides common functionalities for interacting with the EPC (European Payments Council)
 * API, including selecting the appropriate web client (mTLS or simple), injecting authentication tokens,
 * and implementing a retry policy for failed requests.
 * </p>
 * <p>
 * Subclasses should implement request-specific logic while leveraging the provided helper methods
 * to manage API interactions securely and efficiently.
 * </p>
 */
@Slf4j
public abstract class EpcApiInvokerHandler implements RequestHandler<EpcRequest> {

  protected final WebClientFactory webClientFactory;
  protected final OpenAPIClientFactory<DefaultApi> epcClientFactory;
  protected final SepaRequestToPayMapper sepaRequestToPayMapper;
  protected final ServiceProviderConfig serviceProviderConfig;

  /**
   * Constructs a {@code EpcApiInvokerHandler} with required dependencies.
   *
   * @param webClientFactory       Factory for creating web clients (with or without mTLS).
   * @param epcClientFactory       Factory for creating API clients for EPC (European Payments
   *                               Council) communication.
   * @param sepaRequestToPayMapper Mapper for converting RTP requests into EPC-compliant format.
   * @param serviceProviderConfig  Configuration settings for the service provider.
   */
  public EpcApiInvokerHandler(@NonNull final WebClientFactory webClientFactory,
      @NonNull final OpenAPIClientFactory<DefaultApi> epcClientFactory,
      @NonNull final SepaRequestToPayMapper sepaRequestToPayMapper,
      @NonNull final ServiceProviderConfig serviceProviderConfig) {
    this.webClientFactory = Objects.requireNonNull(webClientFactory);
    this.epcClientFactory = Objects.requireNonNull(epcClientFactory);
    this.sepaRequestToPayMapper = Objects.requireNonNull(sepaRequestToPayMapper);
    this.serviceProviderConfig = Objects.requireNonNull(serviceProviderConfig);
  }

  /**
   * Creates an EPC API client based on the provided request. Determines whether to use mTLS or a
   * simple web client based on service provider settings.
   *
   * @param request The EPC request containing service provider details.
   * @return A {@code Mono} containing the created {@code DefaultApi} client.
   */
  @NonNull
  protected Mono<DefaultApi> createEpcClient(@NonNull final EpcRequest request) {
    return Mono.just(request)
        .filter(this::checkMtlsEnabled)
        .doOnNext(req -> log.info("Using mTLS for sending RTP to {}",
            req.rtpToSend().serviceProviderDebtor()))
        .map(req -> this.webClientFactory.createMtlsWebClient())
        .switchIfEmpty(Mono.fromSupplier(() -> {
          log.info("Using simple web client for sending RTP to {}",
              request.rtpToSend().serviceProviderDebtor());
          return this.webClientFactory.createSimpleWebClient();
        }))
        .map(this.epcClientFactory::createClient);
  }

  /**
   * Injects an OAuth2 token into the EPC API request if available.
   *
   * @param epcClient The EPC API client.
   * @param request   The EPC request containing the token and RTP details.
   */
  protected void injectTokenIntoEpcRequest(
      @NonNull final DefaultApi epcClient,
      @NonNull final EpcRequest request) {

    final var rtpToSend = request.rtpToSend();

    Optional.of(request)
        .map(EpcRequest::token)
        .map(StringUtils::trimToNull)
        .ifPresentOrElse(
            token -> {
              log.info("Using OAuth2 token for sending RTP to {}",
                  rtpToSend.serviceProviderDebtor());
              epcClient.getApiClient()
                  .addDefaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            },
            () -> log.info("No OAuth2 token found for sending RTP to {}",
                rtpToSend.serviceProviderDebtor()));
  }

  /**
   * Defines a retry policy for handling failed RTP requests. Uses exponential backoff with jitter
   * to reduce contention in case of failures.
   *
   * @return A {@code RetryBackoffSpec} defining the retry strategy.
   */
  @NonNull
  protected RetryBackoffSpec sendRetryPolicy() {
    final var maxAttempts = serviceProviderConfig.send().retry().maxAttempts();
    final var minDurationMillis = serviceProviderConfig.send().retry().backoffMinDuration();
    final var jitter = serviceProviderConfig.send().retry().backoffJitter();

    return Retry.backoff(maxAttempts, Duration.ofMillis(minDurationMillis))
        .jitter(jitter)
        .doAfterRetry(signal -> log.info("Retry number {}", signal.totalRetries()));
  }

  /**
   * Determines whether mutual TLS (mTLS) should be used for sending the RTP request. It retrieves
   * the configuration from the {@link TechnicalServiceProvider} associated with the given request
   * and checks the `mtlsEnabled` flag. If the flag is absent, it defaults to {@code true}, ensuring
   * secure communication by default.
   *
   * @param request The EPC request containing service provider details.
   * @return {@code true} if mTLS should be used, {@code false} otherwise.
   */
  private boolean checkMtlsEnabled(@NonNull final EpcRequest request) {
    return Optional.of(request)
        .map(EpcRequest::serviceProviderFullData)
        .map(ServiceProviderFullData::tsp)
        .map(TechnicalServiceProvider::mtlsEnabled)
        .orElse(true);
  }
}

