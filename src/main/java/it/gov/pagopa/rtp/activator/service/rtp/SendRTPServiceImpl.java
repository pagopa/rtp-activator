package it.gov.pagopa.rtp.activator.service.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
@Slf4j
public class SendRTPServiceImpl implements SendRTPService {

  private SepaRequestToPayMapper sepaRequestToPayMapper;

  public SendRTPServiceImpl(SepaRequestToPayMapper sepaRequestToPayMapper) {
    this.sepaRequestToPayMapper = sepaRequestToPayMapper;
  }

  @Override
  public Mono<Rtp> send(String noticeNumber, Integer amount, String description, LocalDate expiryDate, String payerId ,String payeeName,
      String payeeId, String rtpSpId, String endToEndId, String iban, String payTrxRef, String flgConf) {

    Rtp rtp = new Rtp(noticeNumber, amount, description, expiryDate, payerId, payeeName, payeeId,
        ResourceID.createNew(), LocalDateTime.now(), rtpSpId, endToEndId, iban, payTrxRef, flgConf);
    // save
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    try { //
      String jsonString = objectMapper.writeValueAsString(sepaRequestToPayMapper.toRequestToPay(rtp));
      log.info(jsonString);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return Mono.just(rtp);
  }
}