package it.gov.pagopa.rtp.activator.repository.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

class RtpDtoMapperTest {

  private RtpMapper rtpMapper;

  @BeforeEach
  void setUp() {
    rtpMapper = new RtpMapper();
  }

  @Test
  void toDomain() {
    var uuid = UUID.randomUUID();
    RtpEntity rtpEntity = RtpEntity.builder()
        .noticeNumber("12345")
        .amount(BigDecimal.valueOf(100.50))
        .description("Test Description")
        .expiryDate(Instant.now())
        .payerId("payer123")
        .payerName("John Doe")
        .payeeName("Payee Name")
        .payeeId("payee123")
        .subject("subject")
        .resourceID(uuid)
        .savingDateTime(Instant.now())
        .rtpSpId("rtpSpId")
        .iban("iban123")
        .payTrxRef("ABC/124")
        .flgConf("Y")
        .status(RtpStatus.CREATED.name())
        .spCreditor("PagoPA")
        .build();

    Rtp rtp = rtpMapper.toDomain(rtpEntity);

    assertNotNull(rtp);
    assertEquals(rtpEntity.getNoticeNumber(), rtp.noticeNumber());
    assertEquals(rtpEntity.getAmount(), rtp.amount());
    assertEquals(rtpEntity.getDescription(), rtp.description());
    assertEquals(LocalDate.ofInstant(rtpEntity.getExpiryDate(), ZoneOffset.UTC), rtp.expiryDate());
    assertEquals(rtpEntity.getPayerId(), rtp.payerId());
    assertEquals(rtpEntity.getPayerName(), rtp.payerName());
    assertEquals(rtpEntity.getPayeeName(), rtp.payeeName());
    assertEquals(rtpEntity.getPayeeId(), rtp.payeeId());
    assertEquals(rtpEntity.getResourceID(), rtp.resourceID().getId());
    assertEquals(LocalDateTime.ofInstant(rtpEntity.getSavingDateTime(), ZoneOffset.UTC), rtp.savingDateTime());
    assertEquals(rtpEntity.getRtpSpId(), rtp.rtpSpId());
    assertEquals(rtpEntity.getIban(), rtp.iban());
    assertEquals(rtpEntity.getPayTrxRef(), rtp.payTrxRef());
    assertEquals(rtpEntity.getFlgConf(), rtp.flgConf());
    assertEquals(rtpEntity.getStatus(), rtp.status().name());
    assertEquals(rtpEntity.getSpCreditor(), rtp.spCreditor());
  }

  @Test
  void toDbEntity() {
    var uuid = UUID.randomUUID();
    Rtp rtp = Rtp.builder()
        .noticeNumber("12345")
        .amount(BigDecimal.valueOf(100.50))
        .description("Test Description")
        .expiryDate(LocalDate.now())
        .payerName("John Doe")
        .payerId("payer123")
        .payeeName("Payee Name")
        .payeeId("payee123")
        .resourceID(new ResourceID(uuid))
        .subject("subject")
        .savingDateTime(LocalDateTime.now())
        .rtpSpId("rtpSpId")
        .iban("iban123")
        .payTrxRef("ABC/124")
        .flgConf("Y")
        .status(RtpStatus.CREATED)
        .spCreditor("PagoPA")
        .build();

    RtpEntity rtpEntity = rtpMapper.toDbEntity(rtp);

    assertNotNull(rtpEntity);
    assertEquals(rtp.noticeNumber(), rtpEntity.getNoticeNumber());
    assertEquals(rtp.amount(), rtpEntity.getAmount());
    assertEquals(rtp.description(), rtpEntity.getDescription());
    assertEquals(rtp.expiryDate().atStartOfDay().toInstant(ZoneOffset.UTC), rtpEntity.getExpiryDate());
    assertEquals(rtp.payerId(), rtpEntity.getPayerId());
    assertEquals(rtp.payerName(), rtpEntity.getPayerName());
    assertEquals(rtp.payeeName(), rtpEntity.getPayeeName());
    assertEquals(rtp.payeeId(), rtpEntity.getPayeeId());
    assertEquals(rtp.resourceID().getId(), rtpEntity.getResourceID());
    assertEquals(rtp.savingDateTime().toInstant(ZoneOffset.UTC), rtpEntity.getSavingDateTime());
    assertEquals(rtp.rtpSpId(), rtpEntity.getRtpSpId());
    assertEquals(rtp.iban(), rtpEntity.getIban());
    assertEquals(rtp.payTrxRef(), rtpEntity.getPayTrxRef());
    assertEquals(rtp.flgConf(), rtpEntity.getFlgConf());
    assertEquals(rtp.status().name(), rtpEntity.getStatus());
    assertEquals(rtp.spCreditor(), rtpEntity.getSpCreditor());
  }
}