package it.gov.pagopa.rtp.activator.service;

import it.gov.pagopa.rtp.activator.model.generated.epc.SepaRequestToPayRequestResourceDto;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import org.springframework.stereotype.Service;

@Service
public class SendRTPService {

  public SepaRequestToPayRequestResourceDto send (CreateRtpDto createRtpDto){
    return new SepaRequestToPayRequestResourceDto();
  }

}
