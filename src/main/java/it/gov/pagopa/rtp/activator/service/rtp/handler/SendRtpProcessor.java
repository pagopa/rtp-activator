package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import lombok.NonNull;
import reactor.core.publisher.Mono;

public interface SendRtpProcessor {

  Mono<Rtp> sendRtpToServiceProviderDebtor(@NonNull final Rtp rtpToSend);

}
