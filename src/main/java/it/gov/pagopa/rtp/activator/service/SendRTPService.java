package it.gov.pagopa.rtp.activator.service;

import it.gov.pagopa.rtp.activator.model.generated.epc.SepaRequestToPayRequestResourceDto;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;

public interface SendRTPService {
    SepaRequestToPayRequestResourceDto send (CreateRtpDto createRtpDto);
}
