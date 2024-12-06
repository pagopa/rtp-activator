package it.gov.pagopa.rtp.activator.service.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SendRTPServiceImpl implements SendRTPService {

  private SepaRequestToPayMapper sepaRequestToPayMapper;

  @Override
  public Mono<Rtp> send(String noticeNumber, Integer amount, String description, LocalDate expiryDate, String payerId,
      String payeeName, String payeeId, String endToEndId, String iban, String payTrxRef, String flgConf) {

    Rtp rtp = new Rtp(noticeNumber, amount, description, expiryDate, payerId, payeeName, payeeId,
        ResourceID.createNew(), LocalDateTime.now(), payeeId, endToEndId, iban, payTrxRef, flgConf);
    //save
    log.info(sepaRequestToPayMapper.toRequestToPay(rtp).toString());
    return Mono.just(rtp);
  }
}