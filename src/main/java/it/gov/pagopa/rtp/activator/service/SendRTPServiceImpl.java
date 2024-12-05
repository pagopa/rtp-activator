package it.gov.pagopa.rtp.activator.service;

import it.gov.pagopa.rtp.activator.model.generated.epc.SepaRequestToPayRequestResourceDto;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class SendRTPServiceImpl implements SendRTPService {

  private SepaRequestToPayMapper sepaRequestToPayMapper;

  public SepaRequestToPayRequestResourceDto send(CreateRtpDto createRtpDto) {

    // Save
    return sepaRequestToPayMapper.toRequestToPay("UUID", LocalDateTime.now(), createRtpDto.getExpiryDate().toString(),
        createRtpDto.getPayerId(), "rtpSpId", "endToEnd", BigDecimal.valueOf(createRtpDto.getAmount()),
        createRtpDto.getPayee().getName(), createRtpDto.getPayee().getPayeeId(), "iban", "placeholder", "placeholder", createRtpDto.getNoticeNumber(),
        createRtpDto.getDescription());

  }

}
