package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.domain.errors.SepaRequestException;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpRepository;
import it.gov.pagopa.rtp.activator.domain.rtp.TransactionStatus;
import it.gov.pagopa.rtp.activator.service.rtp.RtpStatusUpdater;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;


/**
 * The `SendRtpResponseHandler` class is a component responsible for handling the response of an EPC request.
 * It updates the RTP status based on the {@link TransactionStatus} received in the response.
 */
@Component("sendRtpResponseHandler")
@Slf4j
public class SendRtpResponseHandler implements RequestHandler<EpcRequest> {

  private final RtpStatusUpdater rtpStatusUpdater;
  private final RtpRepository rtpRepository;
  private final ServiceProviderConfig serviceProviderConfig;


  /**
   * Constructs a new {@link SendRtpResponseHandler} instance with the provided {@link RtpStatusUpdater}.
   *
   * @param rtpStatusUpdater the {@link RtpStatusUpdater} instance to be used for updating the RTP status
   * @param rtpRepository the {@link RtpRepository} instance to be used for persisting RTP data
   * @param serviceProviderConfig the {@link ServiceProviderConfig} instance containing configuration data for retries
   * @throws NullPointerException if `rtpStatusUpdater` is `null`
   */
  public SendRtpResponseHandler(
      @NonNull final RtpStatusUpdater rtpStatusUpdater,
      @NonNull final RtpRepository rtpRepository,
      @NonNull final ServiceProviderConfig serviceProviderConfig) {
    this.rtpStatusUpdater = Objects.requireNonNull(rtpStatusUpdater);
    this.rtpRepository = Objects.requireNonNull(rtpRepository);
    this.serviceProviderConfig = Objects.requireNonNull(serviceProviderConfig);
  }


  /**
   * Handles the provided {@link EpcRequest} by updating the RTP status based on the transaction status in the response.
   *
   * @param request the {@link EpcRequest} to be handled
   * @return a {@code Mono<EpcRequest>} containing the updated {@link EpcRequest}
   * @throws NullPointerException if `request` is `null`
   */
  @Override
  @NonNull
  public Mono<EpcRequest> handle(@NonNull final EpcRequest request) {
    Objects.requireNonNull(request, "request must not be null");

    return Mono.just(request)
        .doFirst(() -> log.info("Parsing SRTP response"))
        .flatMap(req -> {
          final var rtpToUpdate = req.rtpToSend();
          final var transactionStatus = req.response();

          return Optional.ofNullable(transactionStatus)
              .map(status -> this.triggerRtpStatus(rtpToUpdate, status))
              .orElseGet(() -> this.triggerTransitionAndPersist(rtpToUpdate, this.rtpStatusUpdater::triggerSendRtp));
        })
        .map(request::withRtpToSend);
  }


  /**
   * Triggers the appropriate {@code RTP} status update based on the provided transaction status.
   * If the transaction status is {@link TransactionStatus#RJCT} or {@link TransactionStatus#ERROR},
   * a {@link SepaRequestException} is thrown after state transition.
   *
   * @param rtpToUpdate the {@link Rtp} instance to be updated
   * @param transactionStatus the {@link TransactionStatus} to be used for the update
   * @return a {@code Mono<Rtp>} containing the updated {@link Rtp} instance
   * @throws NullPointerException if {@code rtpToUpdate} or {@code transactionStatus} is {@code null}
   */
  @NonNull
  private Mono<Rtp> triggerRtpStatus(
      @NonNull final Rtp rtpToUpdate,
      @NonNull final TransactionStatus transactionStatus) {
    return switch (transactionStatus) {
      case ACTC -> this.triggerTransitionAndPersist(rtpToUpdate, this.rtpStatusUpdater::triggerAcceptRtp);

      case ACCP -> Mono.error(new IllegalStateException("Not implemented"));

      case RJCT -> this.triggerTransitionAndPersist(rtpToUpdate, this.rtpStatusUpdater::triggerRejectRtp)
          .flatMap(rtp -> Mono.error(new SepaRequestException("SRTP send has been rejected")));

      case ERROR -> this.triggerTransitionAndPersist(rtpToUpdate, this.rtpStatusUpdater::triggerErrorSendRtp)
          .flatMap(rtp -> Mono.error(new SepaRequestException("Could not send SRTP")));
    };
  }


  /**
   * Applies a transition function to an RTP entity and persists the result using retry policy.
   *
   * @param rtpToUpdate       the RTP entity to update
   * @param transitionFunction the function that applies a state transition to the RTP
   * @return a {@code Mono<Rtp>} containing the persisted RTP after transition
   * @throws NullPointerException if either parameter is {@code null}
   */
  @NonNull
  private Mono<Rtp> triggerTransitionAndPersist(
      @NonNull final Rtp rtpToUpdate,
      @NonNull final Function<Rtp, Mono<Rtp>> transitionFunction) {

    Objects.requireNonNull(rtpToUpdate, "rtpToUpdate must not be null");
    Objects.requireNonNull(transitionFunction, "transitionFunction must not be null");

    return transitionFunction.apply(rtpToUpdate)
        .flatMap(
            rtpToSave -> rtpRepository.save(rtpToSave)
                .retryWhen(sendRetryPolicy())
                .doOnError(ex -> log.error("Failed after retries", ex))

        );
  }


  /**
   * Builds a {@link RetryBackoffSpec} for persisting RTP entities using configuration values.
   *
   * @return a configured {@code RetryBackoffSpec} for retrying persistence operations
   */
  private RetryBackoffSpec sendRetryPolicy() {
    final var maxAttempts = serviceProviderConfig.send().retry().maxAttempts();
    final var minDurationMillis = serviceProviderConfig.send().retry().backoffMinDuration();
    final var jitter = serviceProviderConfig.send().retry().backoffJitter();

    return Retry.backoff(maxAttempts, Duration.ofMillis(minDurationMillis))
        .jitter(jitter)
        .doAfterRetry(signal -> log.info("Retry number {}", signal.totalRetries()));
  }
}

