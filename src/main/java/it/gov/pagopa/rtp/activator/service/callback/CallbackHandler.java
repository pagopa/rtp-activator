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

/**
 * Component responsible for handling asynchronous callback responses from SEPA services.
 *
 * <p>This handler extracts relevant fields from the incoming callback JSON,
 * retrieves the corresponding RTP from the repository, applies status transitions,
 * and persists any updates according to the transaction status.
 */
@Component("callbackHandler")
@Slf4j
public class CallbackHandler {

  private final RtpRepository rtpRepository;
  private final RtpStatusUpdater rtpStatusUpdater;
  private final ServiceProviderConfig serviceProviderConfig;
  private final CallbackFieldsExtractor callbackFieldsExtractor;

  /**
   * Constructs a new {@link CallbackHandler} instance.
   *
   * @param rtpRepository the repository to fetch and save RTP records
   * @param rtpStatusUpdater the service to apply status transitions
   * @param serviceProviderConfig configuration parameters for retries
   * @param callbackFieldsExtractor utility to extract fields from JSON callback
   */
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

  /**
   * Handles an incoming callback payload.
   *
   * <p>This method extracts the {@link TransactionStatus} and {@link
   * it.gov.pagopa.rtp.activator.domain.rtp.ResourceID} from the given JSON request body, retrieves
   * the associated {@link Rtp} entity, and applies the appropriate status transitions in sequence.
   * After processing, the original request body is returned.
   *
   * @param requestBody the callback payload received as a JSON object
   * @return a {@link Mono} emitting the original {@code requestBody} upon successful completion
   * @throws IllegalArgumentException or {@link IllegalStateException} in case of parsing or logic
   * errors
   */
  public Mono<JsonNode> handle(@NonNull final JsonNode requestBody) {
    final var transactionStatus = callbackFieldsExtractor.extractTransactionStatusSend(requestBody);
    final var resourceId = callbackFieldsExtractor.extractResourceIDSend(requestBody);

      return resourceId
              .flatMap(rtpRepository::findById)
              .doOnNext(rtp -> log.info("Retrieved RTP with id {}", rtp.resourceID().getId()))
              .flatMap(rtpToUpdate -> transactionStatus
                      .doOnNext(status -> log.debug("Processing transaction status: {}", status))
                      .concatMap(status -> triggerStatus(status, rtpToUpdate))
                      .then(Mono.just(rtpToUpdate))
              )
              .doOnSuccess(r -> log.info("Completed handling callback response"))
              .thenReturn(requestBody);
  }

  /**
   * Triggers the appropriate transition logic based on the given transaction status.
   *
   * @param transactionStatus the status to handle
   * @param rtpToUpdate the RTP entity to transition
   * @return a {@link Mono} containing the updated RTP after applying the transition
   * @throws IllegalStateException if the transaction status is unsupported
   */
  private Mono<Rtp> triggerStatus(@NonNull final TransactionStatus transactionStatus, @NonNull final Rtp rtpToUpdate) {

      log.debug("Handling TransactionStatus: {}", transactionStatus);

      return switch (transactionStatus) {
          case ACCP, ACWC -> {
              log.debug("Triggering ACCEPT transition for RTP {}", rtpToUpdate.resourceID().getId());
              yield this.triggerAndSave(rtpToUpdate, this.rtpStatusUpdater::triggerAcceptRtp);
          }
          case RJCT -> {
              log.debug("Triggering REJECT transition for RTP {}", rtpToUpdate.resourceID().getId());
              yield this.triggerAndSave(rtpToUpdate, this.rtpStatusUpdater::triggerRejectRtp);
          }
          case ERROR -> {
              log.debug("Triggering ERROR transition for RTP {}", rtpToUpdate.resourceID().getId());
              yield this.triggerAndSave(rtpToUpdate, this.rtpStatusUpdater::triggerErrorSendRtp);
          }
          default -> {
              log.warn("Received unsupported TransactionStatus: {}", transactionStatus);
              yield Mono.error(new IllegalStateException("Unsupported TransactionStatus: " + transactionStatus));
          }
      };
  }

  /**
   * Applies the provided transition function and attempts to persist the updated RTP entity.
   *
   * <p>Retry policies are applied to the save operation based on the configuration.
   *
   * @param rtpToUpdate the RTP to update
   * @param transitionFunction the transition function to apply
   * @return a {@link Mono} containing the persisted RTP
   */
  private Mono<Rtp> triggerAndSave(
      @NonNull final Rtp rtpToUpdate, @NonNull final Function<Rtp, Mono<Rtp>> transitionFunction) {

    return transitionFunction.apply(rtpToUpdate)
            .flatMap(rtpToSave -> rtpRepository.save(rtpToSave)
                    .retryWhen(RetryPolicyUtils.sendRetryPolicy(serviceProviderConfig.send().retry()))
                    .doOnError(ex -> log.error("Failed after retries while saving RTP {}", rtpToSave.resourceID().getId(), ex))
            );
  }
}
