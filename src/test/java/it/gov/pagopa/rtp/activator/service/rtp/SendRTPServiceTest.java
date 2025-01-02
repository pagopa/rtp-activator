package it.gov.pagopa.rtp.activator.service.rtp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.model.generated.epc.SepaRequestToPayRequestResourceDto;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SendRTPServiceTest {

    @Mock
    private SepaRequestToPayMapper sepaRequestToPayMapper;

    @InjectMocks
    private SendRTPServiceImpl sendRTPService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSend() {
        String noticeNumber = "12345";
        BigDecimal amount = new BigDecimal("99999999999");
        String description = "Payment Description";
        LocalDate expiryDate = LocalDate.now();
        String payerId = "payerId";
        String payeeName = "Payee Name";
        String payeeId = "payeeId";
        String endToEndId = "endToEndId";
        String rtpSpId = "rtpSpId";
        String iban = "IT60X0542811101000000123456";
        String payTrxRef = "payTrxRef";
        String flgConf = "flgConf";

        Rtp expectedRtp = Rtp.builder().noticeNumber(noticeNumber).amount(amount).description(description)
                .expiryDate(expiryDate)
                .payerId(payerId).payeeName(payeeName).payeeId(payeeId)
                .resourceID(ResourceID.createNew())
                .savingDateTime(LocalDateTime.now()).rtpSpId(rtpSpId).endToEndId(endToEndId)
                .iban(iban).payTrxRef(payTrxRef)
                .flgConf(flgConf).build();
        SepaRequestToPayRequestResourceDto mockSepaRequestToPayRequestResource = new SepaRequestToPayRequestResourceDto(
                URI.create("http://callback.url"));

        when(sepaRequestToPayMapper.toRequestToPay(any(Rtp.class)))
                .thenReturn(mockSepaRequestToPayRequestResource);

        Mono<Rtp> result = sendRTPService.send(expectedRtp);
        StepVerifier.create(result)
                .expectNextMatches(rtp -> rtp.noticeNumber().equals(expectedRtp.noticeNumber())
                        && rtp.amount().equals(expectedRtp.amount())
                        && rtp.description().equals(expectedRtp.description())
                        && rtp.expiryDate().equals(expectedRtp.expiryDate())
                        && rtp.payerId().equals(expectedRtp.payerId())
                        && rtp.payeeName().equals(expectedRtp.payeeName())
                        && rtp.payeeId().equals(expectedRtp.payeeId())
                        && rtp.rtpSpId().equals(expectedRtp.rtpSpId())
                        && rtp.endToEndId().equals(expectedRtp.endToEndId())
                        && rtp.iban().equals(expectedRtp.iban())
                        && rtp.payTrxRef().equals(expectedRtp.payTrxRef())
                        && rtp.flgConf().equals(expectedRtp.flgConf()))
                .verifyComplete();
        verify(sepaRequestToPayMapper, times(1)).toRequestToPay(any(Rtp.class));
    }
}
