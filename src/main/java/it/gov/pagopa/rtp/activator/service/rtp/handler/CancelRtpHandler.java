package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.configuration.OpenAPIClientFactory;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.configuration.mtlswebclient.WebClientFactory;
import it.gov.pagopa.rtp.activator.domain.rtp.TransactionStatus;
import it.gov.pagopa.rtp.activator.epcClient.api.DefaultApi;
import it.gov.pagopa.rtp.activator.service.rtp.SepaRequestToPayMapper;

import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;


/**
 * Handles the cancellation of a Request-to-Pay (RTP) request.
 * This class extends {@link EpcApiInvokerHandler} to interact with the EPC API,
 * sending RTP cancellation requests to the external service provider.
 * It ensures secure communication using mTLS and OAuth2 authentication when required.
 */
@Component("cancelRtpHandler")
@Slf4j
public class CancelRtpHandler extends EpcApiInvokerHandler implements RequestHandler<EpcRequest> {

  /**
   * Constructs a {@code CancelRtpHandler} with required dependencies.
   *
   * @param webClientFactory       Factory for creating web clients (with or without mTLS).
   * @param epcClientFactory       Factory for creating API clients for EPC (European Payments
   *                               Council) communication.
   * @param sepaRequestToPayMapper Mapper for converting RTP cancellation requests into EPC-compliant format.
   * @param serviceProviderConfig  Configuration settings for the service provider.
   */
  public CancelRtpHandler(
      @NonNull final WebClientFactory webClientFactory,
      @NonNull final OpenAPIClientFactory<DefaultApi> epcClientFactory,
      @NonNull final SepaRequestToPayMapper sepaRequestToPayMapper,
      @NonNull final ServiceProviderConfig serviceProviderConfig) {

    super(webClientFactory, epcClientFactory, sepaRequestToPayMapper, serviceProviderConfig);
  }

  /**
   * Handles an incoming EPC request by sending an RTP cancellation request to the external service provider.
   * The request follows multiple steps, including creating an EPC API client, setting API credentials,
   * and handling retries in case of failures.
   *
   * @param request The EPC request containing RTP cancellation details.
   * @return A {@code Mono} containing the updated EPC request with response data.
   */
  @NonNull
  @Override
  public Mono<EpcRequest> handle(@NonNull final EpcRequest request) {
    return this.createEpcClient(request)
        .doOnNext(epcClient -> log.debug("Successfully created EPC client"))
        .flatMap(epcClient -> {
          final var rtpToSend = request.rtpToSend();
          final var sepaRequest = this.sepaRequestToPayMapper.toEpcRequestToCancel(rtpToSend);
          final var basePath = request.serviceProviderFullData().tsp().serviceEndpoint();

          epcClient.getApiClient().setBasePath(basePath);
          this.injectTokenIntoEpcRequest(epcClient, request);

          return Mono.fromCallable(() -> epcClient.postRequestToPayCancellationRequest(
                  request.rtpToSend().resourceID().getId(),
                  UUID.randomUUID().toString(),
                  request.rtpToSend().resourceID().getId().toString(),
                  sepaRequest))
              .doFirst(() -> log.info("Sending RTP cancellation request to {}", rtpToSend.serviceProviderDebtor()))
              .retryWhen(sendRetryPolicy());
        })
        .doOnSuccess(resp -> log.info("Mapping sent RFC to {}", TransactionStatus.CNCL))
        .map(resp -> request.withResponse(TransactionStatus.CNCL))
        .switchIfEmpty(Mono.just(request))
        .onErrorResume(IllegalStateException.class, ex -> this.handleRetryError(ex, request))
        .doOnNext(resp -> log.info("Response: {}", resp.response()));
  }

  /**
   * Handles errors that occur during the retry process of sending an RTP cancellation request. It
   * distinguishes between different types of errors and returns the appropriate transaction status.
   *
   * @param ex The exception thrown during the retry process.
   * @param request The original EPC request.
   * @return A {@code Mono} containing the EPC request with an updated status based on the error
   *     type.
   */
  @NonNull
  private Mono<EpcRequest> handleRetryError(
      @NonNull final IllegalStateException ex, @NonNull final EpcRequest request) {

    log.warn("Handling error upon sending RFC to {}", request.serviceProviderFullData().tsp().serviceEndpoint());

    return Optional.of(ex)
        .filter(Exceptions::isRetryExhausted)
        .map(Throwable::getCause)
        .filter(WebClientResponseException.class::isInstance)
        .map(WebClientResponseException.class::cast)
        .map(WebClientResponseException::getStatusCode)
        .filter(httpStatusCode -> httpStatusCode.isSameCodeAs(HttpStatus.BAD_REQUEST))
        .map(statusCode -> Mono.just(request.withResponse(TransactionStatus.RJCR)))
        .orElse(Mono.just(request.withResponse(TransactionStatus.ERROR)));
  }
}

