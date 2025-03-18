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
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;


/**
 * Handles the process of sending a Request-to-Pay (RTP) request to an external service provider.
 * This class interacts with web clients and API clients to send RTP requests, ensuring secure communication
 * using mutual TLS (mTLS) and OAuth2 authentication when required.
 */
@Component("sendRtpHandler")
@Slf4j
public class SendRtpHandler implements RequestHandler<EpcRequest> {

  private final WebClientFactory webClientFactory;
  private final OpenAPIClientFactory<DefaultApi> epcClientFactory;
  private final SepaRequestToPayMapper sepaRequestToPayMapper;
  private final ServiceProviderConfig serviceProviderConfig;

  /**
   * Constructs a {@code SendRtpHandler} with required dependencies.
   *
   * @param webClientFactory        Factory for creating web clients (with or without mTLS).
   * @param epcClientFactory        Factory for creating API clients for EPC (European Payments Council) communication.
   * @param sepaRequestToPayMapper  Mapper for converting RTP requests into EPC-compliant format.
   * @param serviceProviderConfig   Configuration settings for the service provider.
   */
  public SendRtpHandler(@NonNull final WebClientFactory webClientFactory,
      @NonNull final OpenAPIClientFactory<DefaultApi> epcClientFactory,
      @NonNull final SepaRequestToPayMapper sepaRequestToPayMapper,
      @NonNull final ServiceProviderConfig serviceProviderConfig) {
    this.webClientFactory = Objects.requireNonNull(webClientFactory);
    this.epcClientFactory = Objects.requireNonNull(epcClientFactory);
    this.sepaRequestToPayMapper = Objects.requireNonNull(sepaRequestToPayMapper);
    this.serviceProviderConfig = Objects.requireNonNull(serviceProviderConfig);
  }

  /**
   * Handles an incoming EPC request by sending an RTP request to the external service provider.
   * The request goes through multiple steps, including choosing the appropriate web client (mTLS or simple),
   * setting API credentials, and handling retries in case of failures.
   *
   * @param request The EPC request containing RTP details.
   * @return A {@code Mono} containing the updated EPC request with response data.
   */
  @NonNull
  @Override
  public Mono<EpcRequest> handle(@NonNull final EpcRequest request) {
    final var epcClientMono = Mono.just(request)
        .filter(this::checkMtlsEnabled)
        .doOnNext(req -> log.info("Using mTLS for sending RTP to {}", req.rtpToSend().serviceProviderDebtor()))
        .map(req -> this.webClientFactory.createMtlsWebClient())
        .switchIfEmpty(Mono.fromSupplier(() -> {
          log.info("Using simple web client for sending RTP to {}", request.rtpToSend().serviceProviderDebtor());
          return this.webClientFactory.createSimpleWebClient();
        }))
        .map(this.epcClientFactory::createClient);

    return epcClientMono.doOnNext(epcClient -> log.debug("Successfully created EPC client"))
        .flatMap(epcClient -> {
          final var rtpToSend = request.rtpToSend();
          final var sepaRequest = this.sepaRequestToPayMapper.toEpcRequestToPay(rtpToSend);
          final var basePath = request.serviceProviderFullData().tsp().serviceEndpoint();

          epcClient.getApiClient().setBasePath(basePath);
          Optional.of(request)
              .map(EpcRequest::token)
              .map(StringUtils::trimToNull)
              .ifPresentOrElse(
                  token -> {
                    log.info("Using OAuth2 token for sending RTP to {}", rtpToSend.serviceProviderDebtor());
                    epcClient.getApiClient()
                        .addDefaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                  },
                  () -> log.info("No OAuth2 token found for sending RTP to {}", rtpToSend.serviceProviderDebtor()));

          return Mono.defer(() -> epcClient.postRequestToPayRequests(
                  request.rtpToSend().resourceID().getId(),
                  UUID.randomUUID().toString(),
                  sepaRequest))
              .doFirst(() -> log.info("Sending RTP to {}", rtpToSend.serviceProviderDebtor()))
              .retryWhen(sendRetryPolicy());
        })
        .map(request::withResponse);
  }

  /**
   * Defines a retry policy for handling failed RTP requests.
   * Uses exponential backoff with jitter to reduce contention in case of failures.
   *
   * @return A {@code RetryBackoffSpec} defining the retry strategy.
   */
  @NonNull
  private RetryBackoffSpec sendRetryPolicy() {
    final var maxAttempts = serviceProviderConfig.send().retry().maxAttempts();
    final var minDurationMillis = serviceProviderConfig.send().retry().backoffMinDuration();
    final var jitter = serviceProviderConfig.send().retry().backoffJitter();

    return Retry.backoff(maxAttempts, Duration.ofMillis(minDurationMillis))
        .jitter(jitter)
        .doAfterRetry(signal -> log.info("Retry number {}", signal.totalRetries()));
  }


  /**
   * Determines whether mutual TLS (mTLS) should be used for sending the RTP request.
   * It retrieves the configuration from the {@link TechnicalServiceProvider} associated
   * with the given request and checks the `isMtlsEnabled` flag. If the flag is absent,
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
        .map(TechnicalServiceProvider::isMtlsEnabled)
        .orElse(true);
  }
}

