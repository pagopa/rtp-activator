package it.gov.pagopa.rtp.activator.service.rtp.handler;


import it.gov.pagopa.rtp.activator.domain.errors.RtpInvalidStateTransition;
import it.gov.pagopa.rtp.activator.domain.rtp.*;
import it.gov.pagopa.rtp.activator.service.rtp.RtpStatusUpdater;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Handles the response after attempting to cancel a Request-to-Pay (RTP).
 * This class updates the internal state of the RTP based on the received response
 * and logs the appropriate events.
 */
@Component("cancelRtpResponseHandler")
@Slf4j
public class CancelRtpResponseHandler implements RequestHandler<EpcRequest>{

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
     * Handles an {@code EpcRequest} containing a cancellation response.
     * It processes the RTP status based on the transaction response.
     *
     * @param request The EPC request with the cancellation response.
     * @return A {@code Mono} containing the updated EPC request.
     */
    @Override
    public @NonNull Mono<EpcRequest> handle(@NonNull EpcRequest request) {
        return Mono.just(request)
                .doFirst(() -> log.info("Parsing cancel RTP response"))
                .flatMap(req -> processCancel(req.rtpToSend(), req.response()).map(req::withRtpToSend))
                .doOnSuccess(r -> log.info("Completed handling cancel RTP response"));
    }

    /**
     * Processes the cancellation of an RTP and applies the appropriate status transition.
     *
     * @param rtp The RTP instance to update.
     * @param transactionStatus The transaction status returned from the EPC system.
     * @return A {@code Mono} with the updated RTP.
     */
    private Mono<Rtp> processCancel(Rtp rtp, TransactionStatus transactionStatus) {
        var previousStatus = rtp.status();

        return updater.triggerCancelRtp(rtp)
                .doOnNext(r -> log.info("Successfully triggered cancel RTP"))
                .onErrorMap(IllegalStateException.class,
                        ex -> new RtpInvalidStateTransition(previousStatus.name(), RtpStatus.CANCELLED.name()))
                .flatMap(updated -> Mono.justOrEmpty(transactionStatus)
                        .flatMap(status -> triggerCancelStatus(updated, status))
                        .switchIfEmpty(Mono.just(updated)));
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
