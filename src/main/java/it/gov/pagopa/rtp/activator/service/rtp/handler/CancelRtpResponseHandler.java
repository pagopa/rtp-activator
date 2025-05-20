package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.rtp.*;
import it.gov.pagopa.rtp.activator.service.rtp.RtpStatusUpdater;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

/**
 * Handles the response after attempting to cancel a Request-to-Pay (RTP). This class updates the
 * internal state of the RTP based on the received response and logs the appropriate events.
 */
@Component("cancelRtpResponseHandler")
@Slf4j
public class CancelRtpResponseHandler implements RequestHandler<EpcRequest> {

  private final RtpStatusUpdater updater;

  /**
   * Constructs a {@code CancelRtpResponseHandler} with the given status updater.
   *
   * @param updater The component responsible for updating RTP statuses.
   */
  public CancelRtpResponseHandler(@NonNull RtpStatusUpdater updater) {
    this.updater = Objects.requireNonNull(updater);
  }

  /**
   * Handles the RTP cancellation response contained in the {@code EpcRequest}. This method performs
   * the following operations:
   *
   * <ol>
   *   <li>Extracts the RTP to be updated and the transaction status from the request.
   *   <li>If the transaction status is present, it attempts to trigger the 'CANCELLED' state
   *       transition on the RTP.
   *   <li>Based on the transaction status (CNCL, RJCR, ERROR), it invokes the appropriate status
   *       update action via the {@link RtpStatusUpdater}.
   *   <li>If the transaction status is not present, it simply attempts to trigger the 'CANCELLED'
   *       state transition on the RTP.
   *   <li>Updates the {@code EpcRequest} with the modified RTP.
   * </ol>
   *
   * @param request The {@code EpcRequest} containing the RTP to be updated and the cancellation
   *     response.
   * @return A {@code Mono} that emits the {@code EpcRequest} with the updated RTP.
   * @throws NullPointerException if the provided {@code request} is {@code null}.
   */
  @Override
  public @NonNull Mono<EpcRequest> handle(@NonNull final EpcRequest request) {
    return Mono.just(request)
        .doFirst(() -> log.info("Parsing cancel RTP response"))
        .flatMap(
            req -> {
              final var rtpToUpdate = req.rtpToSend();
              final var transactionStatus = req.response();

              return Optional.ofNullable(transactionStatus)
                  .map(status -> this.updater.triggerCancelRtp(rtpToUpdate)
                              .flatMap(updatedRtp -> triggerCancelStatus(updatedRtp, status)))
                  .orElseGet(() -> this.updater.triggerCancelRtp(rtpToUpdate))
                  .map(request::withRtpToSend)
                  .doOnSuccess(r -> log.info("Completed handling cancel RTP response"));
            });
  }

  /**
   * Applies the appropriate follow-up action depending on the transaction status.
   *
   * @param rtp The RTP to update.
   * @param status The transaction status.
   * @return A {@code Mono} with the updated RTP.
   */
  private Mono<Rtp> triggerCancelStatus(Rtp rtp, TransactionStatus status) {
    log.debug("Handling TransactionStatus: {}", status);

    return switch (status) {
      case CNCL -> updater.triggerCancelRtpAccr(rtp);
      case RJCR -> updater.triggerCancelRtpRejected(rtp);
      case ERROR -> updater.triggerErrorCancelRtp(rtp);
      default -> Mono.error(new IllegalStateException("TransactionStatus not supported: " + status));
    };
  }
}
