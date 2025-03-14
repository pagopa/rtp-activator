package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.configuration.OpenAPIClientFactory;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.configuration.mtlswebclient.WebClientFactory;
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
        .filter(req ->
            StringUtils.trimToNull(req.serviceProviderFullData().tsp().certificateSerialNumber())
                != null)
        .map(req -> this.webClientFactory.createMtlsWebClient())
        .switchIfEmpty(Mono.fromSupplier(this.webClientFactory::createSimpleWebClient))
        .map(this.epcClientFactory::createClient);

    return epcClientMono.flatMap(epcClient -> {
          final var rtpToSend = request.rtpToSend();
          final var sepaRequest = this.sepaRequestToPayMapper.toEpcRequestToPay(rtpToSend);
          final var basePath = request.serviceProviderFullData().tsp().serviceEndpoint();

          epcClient.getApiClient().setBasePath(basePath);
          Optional.of(request)
              .map(EpcRequest::token)
              .map(StringUtils::trimToNull)
              .ifPresent(token ->
                  epcClient.getApiClient()
                      .addDefaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token));

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
}

