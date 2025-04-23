package it.gov.pagopa.rtp.activator.repository.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
import java.math.BigDecimal;
import java.time.Instant;
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


  @Test
  void givenValidId_whenFindById_thenReturnRtp() {
    final var rtpId = UUID.randomUUID();
    final var resourceID = new ResourceID(rtpId);

    final var rtpEntity = RtpEntity.builder()
        .resourceID(rtpId)
        .noticeNumber("12345")
        .amount(BigDecimal.valueOf(100.50))
        .description("Test Description")
        .expiryDate(Instant.now())
        .payerId("payer123")
        .payerName("Payer Name")
        .payeeId("payee123")
        .payeeName("Payee Name")
        .subject("subject")
        .savingDateTime(Instant.now())
        .serviceProviderDebtor("serviceProviderDebtor")
        .iban("iban123")
        .payTrxRef("ABC/124")
        .flgConf("Y")
        .status(RtpStatus.CREATED.name())
        .serviceProviderCreditor("PagoPA")
        .build();

    final var expectedRtp = Rtp.builder()
        .resourceID(resourceID)
        .noticeNumber(rtpEntity.getNoticeNumber())
        .amount(rtpEntity.getAmount())
        .description(rtpEntity.getDescription())
        .expiryDate(LocalDate.ofInstant(rtpEntity.getExpiryDate(), ZoneOffset.UTC))
        .payerId(rtpEntity.getPayerId())
        .payerName(rtpEntity.getPayerName())
        .payeeId(rtpEntity.getPayeeId())
        .payeeName(rtpEntity.getPayeeName())
        .subject(rtpEntity.getSubject())
        .savingDateTime(LocalDateTime.ofInstant(rtpEntity.getSavingDateTime(), ZoneOffset.UTC))
        .serviceProviderDebtor(rtpEntity.getServiceProviderDebtor())
        .iban(rtpEntity.getIban())
        .payTrxRef(rtpEntity.getPayTrxRef())
        .flgConf(rtpEntity.getFlgConf())
        .status(RtpStatus.valueOf(rtpEntity.getStatus()))
        .serviceProviderCreditor(rtpEntity.getServiceProviderCreditor())
        .build();

    when(rtpDB.findById(rtpId)).thenReturn(Mono.just(rtpEntity));

    StepVerifier.create(rtpDbRepository.findById(resourceID))
        .assertNext(actualRtp -> {
          assertEquals(expectedRtp.noticeNumber(), actualRtp.noticeNumber());
          assertEquals(expectedRtp.amount(), actualRtp.amount());
          assertEquals(expectedRtp.description(), actualRtp.description());
          assertEquals(expectedRtp.expiryDate(), actualRtp.expiryDate());
          assertEquals(expectedRtp.payerId(), actualRtp.payerId());
          assertEquals(expectedRtp.payerName(), actualRtp.payerName());
          assertEquals(expectedRtp.payeeId(), actualRtp.payeeId());
          assertEquals(expectedRtp.payeeName(), actualRtp.payeeName());
          assertEquals(expectedRtp.subject(), actualRtp.subject());
          assertEquals(expectedRtp.savingDateTime(), actualRtp.savingDateTime());
          assertEquals(expectedRtp.serviceProviderDebtor(), actualRtp.serviceProviderDebtor());
          assertEquals(expectedRtp.iban(), actualRtp.iban());
          assertEquals(expectedRtp.payTrxRef(), actualRtp.payTrxRef());
          assertEquals(expectedRtp.flgConf(), actualRtp.flgConf());
          assertEquals(expectedRtp.status(), actualRtp.status());
          assertEquals(expectedRtp.serviceProviderCreditor(), actualRtp.serviceProviderCreditor());
          assertEquals(resourceID.getId(), actualRtp.resourceID().getId());
        })
        .verifyComplete();
  }

  @Test
  void givenInvalidId_whenFindById_thenReturnEmpty() {
    final var rtpId = UUID.randomUUID();
    final var resourceID = new ResourceID(rtpId);

    when(rtpDB.findById(rtpId)).thenReturn(Mono.empty());

    StepVerifier.create(rtpDbRepository.findById(resourceID))
        .verifyComplete();
  }

  @Test
  void givenDbError_whenFindById_thenThrowException() {
    final var rtpId = UUID.randomUUID();
    final var resourceID = new ResourceID(rtpId);

    when(rtpDB.findById(rtpId)).thenReturn(Mono.error(new RuntimeException("DB Error")));

    StepVerifier.create(rtpDbRepository.findById(resourceID))
        .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("DB Error"))
        .verify();
  }

}