package it.gov.pagopa.rtp.activator.service.rtp.handler;


import it.gov.pagopa.rtp.activator.domain.errors.RtpInvalidStateTransition;
import it.gov.pagopa.rtp.activator.domain.rtp.*;
import it.gov.pagopa.rtp.activator.service.rtp.RtpStatusUpdater;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Objects;

@Component("cancelRtpResponseHandler")
@Slf4j
public class CancelRtpResponseHandler implements RequestHandler<EpcRequest>{

    private final RtpStatusUpdater updater;

    public CancelRtpResponseHandler(@NonNull RtpStatusUpdater updater) {
        this.updater = Objects.requireNonNull(updater);
    }

    @Override
    public @NonNull Mono<EpcRequest> handle(@NonNull EpcRequest request) {
        return Mono.just(request)
                .doFirst(() -> log.info("Parsing cancel RTP response"))
                .flatMap(req -> processCancel(req.rtpToSend(), req.response()).map(req::withRtpToSend))
                .doOnSuccess(r -> log.info("Completed handling cancel RTP response"));
    }

    private Mono<Rtp> processCancel(Rtp rtp, TransactionStatus transactionStatus) {
        var previousStatus = rtp.status();

        return updater.triggerCancelRtp(rtp)
                .flatMap(updated -> addEvent(updated, RtpEvent.CANCEL_RTP, previousStatus))
                .doOnNext(r -> log.info("Successfully triggered cancel RTP"))
                .onErrorMap(IllegalStateException.class,
                        ex -> new RtpInvalidStateTransition(previousStatus.name(), RtpStatus.CANCELLED.name()))
                .flatMap(updated -> Mono.justOrEmpty(transactionStatus)
                        .flatMap(status -> triggerCancelStatus(updated, status))
                        .switchIfEmpty(Mono.just(updated)));
    }

    private Mono<Rtp> triggerCancelStatus(Rtp rtp, TransactionStatus status) {
        var precStatus = rtp.status();
        log.debug("Handling TransactionStatus: {}", status);

        return switch (status) {
            case CNCL -> updater.triggerCancelRtpAccr(rtp)
                    .flatMap(r -> addEvent(r, RtpEvent.CANCEL_RTP_ACCR, precStatus));
            case RJCR -> updater.triggerCancelRtpRejected(rtp)
                    .flatMap(r -> addEvent(r, RtpEvent.CANCEL_RTP_REJECTED, precStatus));
            case ERROR -> updater.triggerErrorCancelRtp(rtp)
                    .flatMap(r -> addEvent(r, RtpEvent.ERROR_CANCEL_RTP, precStatus));
            default -> Mono.error(new IllegalStateException("TransactionStatus not supported: " + status));
        };
    }

    private Mono<Rtp> addEvent(Rtp rtp, RtpEvent trigger, RtpStatus precStatus) {
        rtp.events().add(Event.builder()
                .timestamp(Instant.now())
                .precStatus(precStatus)
                .triggerEvent(trigger)
                .build());
        return Mono.just(rtp);
    }
}
