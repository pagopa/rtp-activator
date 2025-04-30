package it.gov.pagopa.rtp.activator.service.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import reactor.core.publisher.Mono;

public interface RtpStatusUpdater {

  Mono<Rtp> triggerSendRtp(Rtp rtp);

  Mono<Rtp> triggerCancelRtp(Rtp rtp);

  Mono<Rtp> triggerAcceptRtp(Rtp rtp);

  Mono<Rtp> triggerRejectRtp(Rtp rtp);

  Mono<Rtp> triggerUserAcceptRtp(Rtp rtp);

  Mono<Rtp> triggerUserRejectRtp(Rtp rtp);

  Mono<Rtp> triggerPayRtp(Rtp rtp);

  Mono<Rtp> triggerErrorSendRtp(Rtp rtp);

  Mono<Rtp> triggerErrorCancelRtp(Rtp rtp);

  Mono<Rtp> triggerCancelRtpAccr(Rtp rtp);

  Mono<Rtp> triggerCancelRtpRejected(Rtp rtp);
}

