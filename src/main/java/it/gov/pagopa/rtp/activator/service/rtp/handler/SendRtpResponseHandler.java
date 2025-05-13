package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.TransactionStatus;
import it.gov.pagopa.rtp.activator.service.rtp.RtpStatusUpdater;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * The `SendRtpResponseHandler` class is a component responsible for handling the response of an EPC request.
 * It updates the RTP status based on the {@link TransactionStatus} received in the response.
 */
@Component("sendRtpResponseHandler")
@Slf4j
public class SendRtpResponseHandler implements RequestHandler<EpcRequest> {

  private final RtpStatusUpdater rtpStatusUpdater;


  /**
   * Constructs a new {@link SendRtpResponseHandler} instance with the provided {@link RtpStatusUpdater}.
   *
   * @param rtpStatusUpdater the {@link RtpStatusUpdater} instance to be used for updating the RTP status
   * @throws NullPointerException if `rtpStatusUpdater` is `null`
   */
  public SendRtpResponseHandler(@NonNull final RtpStatusUpdater rtpStatusUpdater) {
    this.rtpStatusUpdater = Objects.requireNonNull(rtpStatusUpdater);
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
              .orElseGet(() -> this.rtpStatusUpdater.triggerSendRtp(rtpToUpdate));
        })
        .map(request::withRtpToSend);
  }


  /**
   * Triggers the appropriate {@code RTP} status update based on the provided transaction status.
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
      case ACTC -> this.rtpStatusUpdater.triggerAcceptRtp(rtpToUpdate);
      case ACCP -> Mono.error(new IllegalStateException("Not implemented"));
      case RJCT -> this.rtpStatusUpdater.triggerRejectRtp(rtpToUpdate);
      case ERROR -> this.rtpStatusUpdater.triggerErrorSendRtp(rtpToUpdate);
    };
  }
}

