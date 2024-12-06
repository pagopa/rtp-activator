package it.gov.pagopa.rtp.activator.service.rtp;

import java.time.LocalDate;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import reactor.core.publisher.Mono;


public interface SendRTPService {
    Mono<Rtp> send (String noticeNumber, Integer amount, String description, LocalDate expiryDate, String payerId,
    String payeeName, String payeeId, String endToEndId, String iban, String payTrxRef, String flgConf);
}
