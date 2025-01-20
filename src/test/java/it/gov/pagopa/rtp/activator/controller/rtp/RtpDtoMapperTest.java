package it.gov.pagopa.rtp.activator.controller.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import it.gov.pagopa.rtp.activator.model.generated.send.PayeeDto;
import it.gov.pagopa.rtp.activator.model.generated.send.PayerDto;
import it.gov.pagopa.rtp.activator.model.generated.send.PaymentNoticeDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class RtpDtoMapperTest {
    private RtpDtoMapper rtpDtoMapper;

    @BeforeEach
    void setUp() {
        rtpDtoMapper = new RtpDtoMapper();
    }

  @Test
  void testToRtp() {
    CreateRtpDto createRtpDto = new CreateRtpDto();
    PaymentNoticeDto paymentNoticeDto = new PaymentNoticeDto();
    PayeeDto payeeDto = new PayeeDto();
    PayerDto payerDto = new PayerDto();

    paymentNoticeDto.setAmount(BigDecimal.valueOf(100));
    paymentNoticeDto.setNoticeNumber("12345");
    paymentNoticeDto.setDescription("Payment Description");
    paymentNoticeDto.setExpiryDate(LocalDate.now());
    paymentNoticeDto.setSubject("Subject");
    createRtpDto.setPaymentNotice(paymentNoticeDto);
    payerDto.setPayerId("payer123");
    payerDto.setName("John Doe");
    createRtpDto.setPayer(payerDto);
    payeeDto.setPayeeId("payee123");
    payeeDto.setName("Payee Name");
    payeeDto.setPayTrxRef("ABC/124");
    createRtpDto.setPayee(payeeDto);
    Rtp rtp = rtpDtoMapper.toRtp(createRtpDto);
    assertThat(rtp).isNotNull();
    assertThat(rtp.resourceID()).isNotNull();
    assertThat(rtp.savingDateTime()).isNotNull();
    assertThat(rtp.noticeNumber()).isEqualTo(createRtpDto.getPaymentNotice().getNoticeNumber());
    assertThat(rtp.amount()).isEqualTo(createRtpDto.getPaymentNotice().getAmount());
    assertThat(rtp.description()).isEqualTo(createRtpDto.getPaymentNotice().getDescription());
    assertThat(rtp.expiryDate()).isEqualTo(createRtpDto.getPaymentNotice().getExpiryDate());
    assertThat(rtp.payerName()).isEqualTo(createRtpDto.getPayer().getName());
    assertThat(rtp.subject()).isEqualTo(createRtpDto.getPaymentNotice().getSubject());
    assertThat(rtp.payerId()).isEqualTo(createRtpDto.getPayer().getPayerId());
    assertThat(rtp.payeeName()).isEqualTo(createRtpDto.getPayee().getName());
    assertThat(rtp.payeeId()).isEqualTo(createRtpDto.getPayee().getPayeeId());
    assertThat(rtp.rtpSpId()).isEqualTo("rtpSpId");
    assertThat(rtp.endToEndId()).isEqualTo("endToEndId");
    assertThat(rtp.iban()).isEqualTo("iban");
    assertThat(rtp.payTrxRef()).isEqualTo(createRtpDto.getPayee().getPayTrxRef());
    assertThat(rtp.flgConf()).isEqualTo("flgConf");
  }
}