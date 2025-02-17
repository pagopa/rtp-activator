package it.gov.pagopa.rtp.activator.controller.rtp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import it.gov.pagopa.rtp.activator.configuration.SecurityConfig;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.domain.errors.MessageBadFormed;
import it.gov.pagopa.rtp.activator.domain.errors.PayerNotActivatedException;
import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorsDto;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import it.gov.pagopa.rtp.activator.model.generated.send.PayeeDto;
import it.gov.pagopa.rtp.activator.model.generated.send.PayerDto;
import it.gov.pagopa.rtp.activator.model.generated.send.PaymentNoticeDto;
import it.gov.pagopa.rtp.activator.service.rtp.SendRTPService;
import it.gov.pagopa.rtp.activator.utils.Users.RtpSenderWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = {SendAPIControllerImpl.class})
@Import({SecurityConfig.class})
@DisabledInAotMode
class SendAPIControllerImplTest {

  @MockitoBean
  private SendRTPService sendRTPService;

  @MockitoBean
  private RtpDtoMapper rtpDtoMapper;

  @Autowired
  private ServiceProviderConfig serviceProviderConfig;

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
    String rtpSpId = "serviceProviderDebtor";
    String iban = "IT60X0542811101000000123456";
    String flgConf = "flgConf";
    String payerName = "John Doe";
    String payTrxRef = "ABC/124";
    String subject = "subject";
    String serviceProviderCreditor = "Pagopa";

    expectedRtp = Rtp.builder().noticeNumber(noticeNumber).amount(amount).description(description)
        .expiryDate(expiryDate)
        .payerId(payerId).payeeName(payeeName).payeeId(payeeId)
        .resourceID(ResourceID.createNew())
        .savingDateTime(LocalDateTime.now()).serviceProviderDebtor(rtpSpId)
        .payerName(payerName)
        .subject(subject)
        .serviceProviderCreditor(serviceProviderCreditor)
        .iban(iban).payTrxRef(payTrxRef)
        .flgConf(flgConf).build();

    webTestClient = WebTestClient
        .bindToApplicationContext(context)
        .apply(springSecurity())
        .configureClient()
        .build();
  }

  @Test
  @RtpSenderWriter()
  void testSendRtpSuccessful() {

    when(rtpDtoMapper.toRtpWithServiceProviderCreditor(any(CreateRtpDto.class),
        eq("PagoPA"))).thenReturn(expectedRtp);
    when(sendRTPService.send(expectedRtp)).thenReturn(Mono.just(expectedRtp));

    webTestClient
        .post()
        .uri("/rtps")
        .bodyValue(generateSendRequest())
        .exchange()
        .expectStatus()
        .isCreated()
        .expectHeader()
        .location("http://localhost:8080/rtp/rtps/" + expectedRtp.resourceID().getId())
        .expectBody()
        .isEmpty();

    verify(sendRTPService, times(1)).send(expectedRtp);
  }

  @Test
  @RtpSenderWriter
  void testSendRtpWithWrongBody() {

    when(rtpDtoMapper.toRtpWithServiceProviderCreditor(any(CreateRtpDto.class),
        anyString())).thenReturn(expectedRtp);
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
  @RtpSenderWriter
  void testSendRtpWithWrongAmount() {

    when(rtpDtoMapper.toRtpWithServiceProviderCreditor(any(CreateRtpDto.class),
        anyString())).thenReturn(expectedRtp);
    when(sendRTPService.send(any()))
        .thenReturn(Mono.empty());

    webTestClient.post()
        .uri("/rtps")
        .bodyValue(generateWrongAmountSendRequest())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST);

    verify(sendRTPService, times(0)).send(any());
    verify(rtpDtoMapper, times(0)).toRtp(any());
  }

  @Test
  @WithMockUser
  void userWithoutEnoughPermissionShouldNotSendRtp() {
    webTestClient.post()
        .uri("/rtps")
        .bodyValue(generateSendRequest())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @RtpSenderWriter
  void givenUserNotActivatedWhenSendRTPThenReturnUnprocessableEntity() {

    when(rtpDtoMapper.toRtpWithServiceProviderCreditor(any(CreateRtpDto.class),
        anyString())).thenReturn(expectedRtp);
    when(sendRTPService.send(any()))
        .thenReturn(Mono.error(new PayerNotActivatedException()));

    webTestClient.post()
        .uri("/rtps")
        .bodyValue(generateSendRequest())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

    verify(sendRTPService, times(1)).send(any());
    verify(rtpDtoMapper, times(1)).toRtpWithServiceProviderCreditor(any(), (any()));
  }

  @Test
  @RtpSenderWriter
  void givenMessageBadFormedWhenSendRTPThenReturnBadRequest() {

    when(rtpDtoMapper.toRtpWithServiceProviderCreditor(any(CreateRtpDto.class),
        anyString())).thenReturn(expectedRtp);
    when(sendRTPService.send(any()))
        .thenReturn(Mono.error(generateMessageBadFormed()));

    webTestClient.post()
        .uri("/rtps")
        .bodyValue(generateSendRequest())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST);

    verify(sendRTPService, times(1)).send(any());
    verify(rtpDtoMapper, times(1)).toRtpWithServiceProviderCreditor(any(), (any()));
  }

  @Test
  @RtpSenderWriter
  void givenBadFiscalCodeWhenSendRTPThenReturnBadRequest() {

    webTestClient.post()
        .uri("/rtps")
        .bodyValue(generateWrongSendRequest())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.BAD_REQUEST);

    verify(sendRTPService, times(0)).send(any());
    verify(rtpDtoMapper, times(0)).toRtpWithServiceProviderCreditor(any(), (any()));
  }

  @Test
  @RtpSenderWriter
  void givenBadExpiryDate_whenSendRTP_thenReturnBadRequest() {

    String invalidJson = """
        {
            "payee": {
                "name": "Comune di Smartino",
                "payeeId": "77777777777",
                "payTrxRef": "ABC/124"
            },
            "payer": {
                "name": "Pigrolo",
                "payerId": "NNAPRL01D01H501T"
            },
            "paymentNotice": {
                "noticeNumber": "311111111112222222",
                "description": "Paga questo avviso",
                "subject": "TARI 2025",
                "amount": 40000,
                "expiryDate": "UNPARSABLE"
            }
        }
        """;

    // When: Sending a POST request with invalid type
    webTestClient.post()
        .uri("/rtps")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(invalidJson)
        .exchange()
        // Then: Verify the response
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.error").isEqualTo("Malformed request")
        .jsonPath("$.details").exists();

    verify(sendRTPService, times(0)).send(any());
    verify(rtpDtoMapper, times(0)).toRtpWithServiceProviderCreditor(any(), (any()));

  }

  private CreateRtpDto generateSendRequest() {

    PayeeDto payeeDto = new PayeeDto();

    PayerDto payerDto = new PayerDto();

    PaymentNoticeDto paymentNoticeDto = new PaymentNoticeDto();

    payeeDto.setName("payeeName");
    payeeDto.setPayeeId("77777777777");
    payeeDto.setPayTrxRef("ABC/124");

    payerDto.setName("payerName");
    payerDto.setPayerId("12345678911");

    paymentNoticeDto.setAmount(BigDecimal.valueOf(1));
    paymentNoticeDto.setSubject("subject");
    paymentNoticeDto.setDescription("description");
    paymentNoticeDto.setNoticeNumber("311111111112222222");
    paymentNoticeDto.setExpiryDate(LocalDate.now());

    return new CreateRtpDto(payeeDto, payerDto, paymentNoticeDto);
  }

  private CreateRtpDto generateWrongSendRequest() {
    PayeeDto payeeDto = new PayeeDto();

    PayerDto payerDto = new PayerDto();

    PaymentNoticeDto paymentNoticeDto = new PaymentNoticeDto();

    payeeDto.setName("payeeName");
    payeeDto.setPayeeId("77777777777");
    payeeDto.setPayTrxRef("ABC/124");

    payerDto.setName("payername");
    payerDto.setPayerId("badfiscalcode");

    paymentNoticeDto.setAmount(BigDecimal.valueOf(1));
    paymentNoticeDto.setSubject("subject");
    paymentNoticeDto.setDescription("description");
    paymentNoticeDto.setNoticeNumber("noticenumber");
    paymentNoticeDto.setExpiryDate(LocalDate.now());

    return new CreateRtpDto(payeeDto, payerDto, paymentNoticeDto);
  }

  private CreateRtpDto generateWrongAmountSendRequest() {
    PayeeDto payeeDto = new PayeeDto();

    PayerDto payerDto = new PayerDto();

    PaymentNoticeDto paymentNoticeDto = new PaymentNoticeDto();

    payeeDto.setName("payeeName");
    payeeDto.setPayeeId("77777777777");
    payeeDto.setPayTrxRef("ABC/124");

    payerDto.setName("payername");
    payerDto.setPayerId("payerId");

    paymentNoticeDto.setAmount(new BigDecimal("999999999999"));
    paymentNoticeDto.setSubject("subject");
    paymentNoticeDto.setDescription("description");
    paymentNoticeDto.setNoticeNumber("311111111112222222");
    paymentNoticeDto.setExpiryDate(LocalDate.now());

    return new CreateRtpDto(payeeDto, payerDto, paymentNoticeDto);

  }

  private MessageBadFormed generateMessageBadFormed() {
    var errors = new ErrorsDto();
    var newError = new ErrorDto();
    newError.setDescription("description");
    newError.setCode("code");
    errors.addErrorsItem(newError);
    return new MessageBadFormed(errors);
  }
}
