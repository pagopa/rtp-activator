package it.gov.pagopa.rtp.activator.service.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import reactor.core.publisher.Mono;

public interface SendRTPService {
  Mono<Rtp> send(Rtp rtp);
}
