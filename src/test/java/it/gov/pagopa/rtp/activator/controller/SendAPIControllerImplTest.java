package it.gov.pagopa.rtp.activator.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import it.gov.pagopa.rtp.activator.configuration.SecurityConfig;
import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpMapper;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import it.gov.pagopa.rtp.activator.model.generated.send.PayeeDto;
import it.gov.pagopa.rtp.activator.service.rtp.SendRTPService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = { SendAPIControllerImpl.class })
@Import({ SecurityConfig.class })
@DisabledInAotMode
class SendAPIControllerImplTest {

    @MockBean
    private SendRTPService sendRTPService;

    @MockBean
    private RtpMapper rtpMapper; 

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext context;

    private Rtp expectedRtp;

    @BeforeEach
    void setup() {
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

        expectedRtp = Rtp.builder().noticeNumber(noticeNumber).amount(amount).description(description)
                .expiryDate(expiryDate)
                .payerId(payerId).payeeName(payeeName).payeeId(payeeId)
                .resourceID(ResourceID.createNew())
                .savingDateTime(LocalDateTime.now()).rtpSpId(rtpSpId).endToEndId(endToEndId)
                .iban(iban).payTrxRef(payTrxRef)
                .flgConf(flgConf).build();


        webTestClient = WebTestClient
                .bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    void testSendRtpSuccessful() {

        when(rtpMapper.toRtp(any(CreateRtpDto.class))).thenReturn(expectedRtp); 
        when(sendRTPService.send(expectedRtp)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/rtps")
                .bodyValue(generateSendRequest())
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .isEmpty();
    }

    @Test
    void testSendRtpWithWrongBody() {

        when(rtpMapper.toRtp(any(CreateRtpDto.class))).thenReturn(expectedRtp); 
        when(sendRTPService.send(any()))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/rtps")
                .bodyValue(generateWrongSendRequest())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    void testSendRtpWithWrongAmount() {

        when(rtpMapper.toRtp(any(CreateRtpDto.class))).thenReturn(expectedRtp); 
        when(sendRTPService.send(any()))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/rtps")
                .bodyValue(generateWrongAmountSendRequest())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private CreateRtpDto generateSendRequest() {
        return new CreateRtpDto("311111111112222222", BigDecimal.valueOf(1), "description", LocalDate.now(),
                "payerId",
                new PayeeDto("77777777777", "payeeName"));
    }

    private CreateRtpDto generateWrongSendRequest() {
        return new CreateRtpDto("noticenumber", BigDecimal.valueOf(1), "description", LocalDate.now(),
                "payerId",
                new PayeeDto("dsds", "payeeName"));
    }

    private CreateRtpDto generateWrongAmountSendRequest() {
        return new CreateRtpDto("311111111112222222", new BigDecimal("999999999999"), "description", LocalDate.now(),
                "payerId",
                new PayeeDto("77777777777", "payeeName"));
    }
}
