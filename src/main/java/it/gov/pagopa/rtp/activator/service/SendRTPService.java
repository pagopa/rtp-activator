package it.gov.pagopa.rtp.activator.service;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import reactor.core.publisher.Mono;


public interface SendRTPService {
    Mono<Rtp> send (CreateRtpDto createRtpDto);
}
