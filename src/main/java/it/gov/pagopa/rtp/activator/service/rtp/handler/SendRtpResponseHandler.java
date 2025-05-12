package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.service.rtp.RtpStatusUpdater;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component("sendRtpResponseHandler")
@Slf4j
public class SendRtpResponseHandler implements RequestHandler<EpcRequest> {

  private final RtpStatusUpdater rtpStatusUpdater;


  public SendRtpResponseHandler(
      @NonNull final RtpStatusUpdater rtpStatusUpdater) {
    this.rtpStatusUpdater = Objects.requireNonNull(rtpStatusUpdater);
  }


  @Override
  @NonNull
  public Mono<EpcRequest> handle(@NonNull final EpcRequest request) {
    Objects.requireNonNull(request, "request must not be null");

    return Mono.just(request)
        .doFirst(() -> log.info("Parsing SRTP response"))
        .flatMap(req -> {
          final var rtpToUpdate = req.rtpToSend();
          final var transactionStatus = req.response();

          if (transactionStatus == null)
            return this.rtpStatusUpdater.triggerSendRtp(rtpToUpdate);

          return switch (transactionStatus) {
            case ACTC -> this.rtpStatusUpdater.triggerAcceptRtp(rtpToUpdate);
            case ACCP -> Mono.error(new IllegalStateException("Not implemented"));
            case RJCT -> this.rtpStatusUpdater.triggerRejectRtp(rtpToUpdate);
            case ERROR -> this.rtpStatusUpdater.triggerErrorSendRtp(rtpToUpdate);
          };
        })
        .map(request::withRtpToSend);
  }
}
