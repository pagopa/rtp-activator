package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.configuration.OpenAPIClientFactory;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.configuration.mtlswebclient.MtlsWebClientFactory;
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


@Component("sendRtpHandler")
@Slf4j
public class SendRtpHandler implements RequestHandler<EpcRequest> {

  private final MtlsWebClientFactory webClientFactory;
  private final OpenAPIClientFactory<DefaultApi> epcClientFactory;
  private final SepaRequestToPayMapper sepaRequestToPayMapper;
  private final ServiceProviderConfig serviceProviderConfig;


  public SendRtpHandler(@NonNull final MtlsWebClientFactory webClientFactory,
      @NonNull final OpenAPIClientFactory<DefaultApi> epcClientFactory,
      @NonNull final SepaRequestToPayMapper sepaRequestToPayMapper,
      @NonNull final ServiceProviderConfig serviceProviderConfig) {

    this.webClientFactory = Objects.requireNonNull(webClientFactory);
    this.epcClientFactory = Objects.requireNonNull(epcClientFactory);
    this.sepaRequestToPayMapper = Objects.requireNonNull(sepaRequestToPayMapper);
    this.serviceProviderConfig = Objects.requireNonNull(serviceProviderConfig);
  }


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
