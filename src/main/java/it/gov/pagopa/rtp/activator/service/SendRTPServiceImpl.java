package it.gov.pagopa.rtp.activator.service;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.model.generated.epc.SepaRequestToPayRequestResourceDto;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import reactor.core.publisher.Mono;


import org.springframework.stereotype.Service;

@Service
public class SendRTPServiceImpl implements SendRTPService {

  @Override
  public Mono<Rtp> send(CreateRtpDto createRtpDto) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'send'");
  }

}
