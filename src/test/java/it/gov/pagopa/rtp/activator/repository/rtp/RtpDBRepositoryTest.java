package it.gov.pagopa.rtp.activator.repository.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RtpDBRepositoryTest {

  @Mock
  private RtpDB rtpDB;
  private final RtpMapper rtpMapper = new RtpMapper();
  private RtpDBRepository rtpDbRepository;

  @BeforeEach
  void setUp() {
    rtpDbRepository = new RtpDBRepository(rtpDB, rtpMapper);
  }


  @Test
  void testSaveRtp() {
    Rtp rtp = Rtp.builder()
        .noticeNumber("12345")
        .amount(BigDecimal.valueOf(100.50))
        .description("Test Description")
        .expiryDate(LocalDate.now())
        .payerId("payer123")
        .payerName("John Doe")
        .payeeName("Payee Name")
        .payeeId("payee123")
        .subject("subject")
        .resourceID(new ResourceID(UUID.randomUUID()))
        .savingDateTime(LocalDateTime.now())
        .serviceProviderDebtor("serviceProviderDebtor")
        .iban("iban123")
        .payTrxRef("ABC/124")
        .flgConf("Y")
        .status(RtpStatus.CREATED)
        .serviceProviderCreditor("PagoPA")
        .build();

    RtpEntity rtpEntity = RtpEntity.builder()
        .noticeNumber(rtp.noticeNumber())
        .amount(rtp.amount())
        .description(rtp.description())
        .expiryDate(rtp.expiryDate().atStartOfDay().toInstant(ZoneOffset.UTC))
        .payerId(rtp.payerId())
        .payeeName(rtp.payeeName())
        .payeeId(rtp.payeeId())
        .resourceID(rtp.resourceID().getId())
        .savingDateTime(rtp.savingDateTime().toInstant(ZoneOffset.UTC))
        .serviceProviderDebtor(rtp.serviceProviderDebtor())
        .iban(rtp.iban())
        .payTrxRef(rtp.payTrxRef())
        .flgConf(rtp.flgConf())
        .status("CREATED")
        .serviceProviderCreditor("PagoPA")
        .build();

    when(rtpDB.save(any())).thenReturn(Mono.just(rtpEntity));

    var savedRtpMono = rtpDbRepository.save(rtp);

    StepVerifier.create(savedRtpMono)
        .assertNext(savedRtp -> {
          assertNotNull(savedRtp);
          assertEquals(rtp.noticeNumber(), savedRtp.noticeNumber());
          assertEquals(rtp.amount(), savedRtp.amount());
          assertEquals(rtp.description(), savedRtp.description());
          assertEquals(rtp.expiryDate(), savedRtp.expiryDate());
          assertEquals(rtp.payerId(), savedRtp.payerId());
          assertEquals(rtp.payeeName(), savedRtp.payeeName());
          assertEquals(rtp.payeeId(), savedRtp.payeeId());
          assertEquals(rtp.resourceID().getId(), savedRtp.resourceID().getId());
          assertEquals(rtp.savingDateTime(), savedRtp.savingDateTime());
          assertEquals(rtp.serviceProviderDebtor(), savedRtp.serviceProviderDebtor());
          assertEquals(rtp.iban(), savedRtp.iban());
          assertEquals(rtp.payTrxRef(), savedRtp.payTrxRef());
          assertEquals(rtp.flgConf(), savedRtp.flgConf());
          assertEquals(rtp.status(), savedRtp.status());
          assertEquals(rtp.serviceProviderCreditor(), rtpEntity.getServiceProviderCreditor());
        })
        .verifyComplete();
  }
}