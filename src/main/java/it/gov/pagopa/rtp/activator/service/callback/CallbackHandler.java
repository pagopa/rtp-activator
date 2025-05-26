package it.gov.pagopa.rtp.activator.service.callback;

import com.fasterxml.jackson.databind.JsonNode;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpRepository;
import it.gov.pagopa.rtp.activator.domain.rtp.TransactionStatus;
import it.gov.pagopa.rtp.activator.service.rtp.RtpStatusUpdater;
import it.gov.pagopa.rtp.activator.utils.RetryPolicyUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

@Component("callbackHandler")
@Slf4j
public class CallbackHandler {

  private final RtpRepository rtpRepository;
  private final RtpStatusUpdater rtpStatusUpdater;
  private final ServiceProviderConfig serviceProviderConfig;
  private final CallbackFieldsExtractor callbackFieldsExtractor;

  public CallbackHandler(
          @NonNull RtpRepository rtpRepository,
          @NonNull RtpStatusUpdater rtpStatusUpdater,
          @NonNull ServiceProviderConfig serviceProviderConfig,
          @NonNull CallbackFieldsExtractor callbackFieldsExtractor) {
    this.rtpRepository = Objects.requireNonNull(rtpRepository);
    this.rtpStatusUpdater = Objects.requireNonNull(rtpStatusUpdater);
    this.serviceProviderConfig = Objects.requireNonNull(serviceProviderConfig);
    this.callbackFieldsExtractor = Objects.requireNonNull(callbackFieldsExtractor);
  }

  public Mono<JsonNode> handle(@NonNull final JsonNode requestBody) {
    final var transactionStatus = callbackFieldsExtractor.extractTransactionStatusSend(requestBody);
    final var resourceId = callbackFieldsExtractor.extractResourceIDSend(requestBody);

      return resourceId
              .flatMap(rtpRepository::findById)
              .doOnNext(rtp -> log.info("Retrieved RTP with id {}", rtp.resourceID().getId()))
              .flatMap(rtpToUpdate -> transactionStatus
                      .concatMap(status -> triggerStatus(status, rtpToUpdate))
                      .then(Mono.just(rtpToUpdate))
              )
              .doOnSuccess(r -> log.info("Completed handling callback response"))
              .thenReturn(requestBody);
  }

  private Mono<Rtp> triggerStatus(
      @NonNull final TransactionStatus transactionStatus,
      @NonNull final Rtp rtpToUpdate) {

      log.debug("Handling TransactionStatus: {}", transactionStatus);

      return switch (transactionStatus) {
          case ACCP, ACWC -> this.triggerAndSave(rtpToUpdate, this.rtpStatusUpdater::triggerAcceptRtp);
          case RJCT -> this.triggerAndSave(rtpToUpdate, this.rtpStatusUpdater::triggerRejectRtp);
          case ERROR -> this.triggerAndSave(rtpToUpdate, this.rtpStatusUpdater::triggerErrorSendRtp);
          default -> Mono.error(new IllegalStateException("Unsupported TransactionStatus: " + transactionStatus));
      };
  }

  private Mono<Rtp> triggerAndSave(@NonNull final Rtp rtpToUpdate,
                                   @NonNull final Function<Rtp, Mono<Rtp>> transitionFunction) {

    return transitionFunction.apply(rtpToUpdate)
            .flatMap(rtpToSave -> rtpRepository.save(rtpToSave)
                    .retryWhen(RetryPolicyUtils.sendRetryPolicy(serviceProviderConfig.send().retry()))
                    .doOnError(ex -> log.error("Failed after retries", ex))
            );
  }
}
