package it.gov.pagopa.rtp.activator.controller;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import it.gov.pagopa.rtp.activator.model.generated.send.PayeeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class RtpMapperTest {
    private RtpMapper rtpMapper;

    @BeforeEach
    void setUp() {
        rtpMapper = new RtpMapper();
    }

    @Test
    void testToRtp() {
        CreateRtpDto createRtpDto = new CreateRtpDto();
        createRtpDto.setNoticeNumber("12345");
        createRtpDto.setAmount(BigDecimal.valueOf(100));
        createRtpDto.setDescription("Payment Description");
        createRtpDto.setExpiryDate(LocalDate.now());
        createRtpDto.setPayerId("payer123");
        PayeeDto payeeDto = new PayeeDto();
        payeeDto.setPayeeId("payee123");
        payeeDto.setName("Payee Name");
        createRtpDto.setPayee(payeeDto);
        Rtp rtp = rtpMapper.toRtp(createRtpDto);
        assertThat(rtp).isNotNull();
        assertThat(rtp.resourceID()).isNotNull();
        assertThat(rtp.savingDateTime()).isNotNull();
        assertThat(rtp.noticeNumber()).isEqualTo(createRtpDto.getNoticeNumber());
        assertThat(rtp.amount()).isEqualTo(createRtpDto.getAmount());
        assertThat(rtp.description()).isEqualTo(createRtpDto.getDescription());
        assertThat(rtp.expiryDate()).isEqualTo(createRtpDto.getExpiryDate());
        assertThat(rtp.payerId()).isEqualTo(createRtpDto.getPayerId());
        assertThat(rtp.payeeName()).isEqualTo(createRtpDto.getPayee().getName());
        assertThat(rtp.payeeId()).isEqualTo(createRtpDto.getPayee().getPayeeId());
        assertThat(rtp.rtpSpId()).isEqualTo("rtpSpId");
        assertThat(rtp.endToEndId()).isEqualTo("endToEndId");
        assertThat(rtp.iban()).isEqualTo("iban");
        assertThat(rtp.payTrxRef()).isEqualTo("payTrxRef");
        assertThat(rtp.flgConf()).isEqualTo("flgConf");
    }
}