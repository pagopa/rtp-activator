package it.gov.pagopa.rtp.activator.domain.rtp;

import reactor.core.publisher.Mono;

public interface RtpRepository {

  Mono<Rtp> save(Rtp rtp);

}
