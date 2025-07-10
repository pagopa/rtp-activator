package it.gov.pagopa.rtp.activator.controller.activation;

import static it.gov.pagopa.rtp.activator.utils.Users.SERVICE_PROVIDER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import it.gov.pagopa.rtp.activator.configuration.ActivationPropertiesConfig;
import it.gov.pagopa.rtp.activator.configuration.SecurityConfig;
import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import it.gov.pagopa.rtp.activator.domain.errors.PayerNotFoundException;
import it.gov.pagopa.rtp.activator.domain.payer.ActivationID;
import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationReqDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.ErrorsDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PageMetadataDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PageOfActivationsDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PayerDto;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationDBRepository;
import it.gov.pagopa.rtp.activator.service.activation.ActivationPayerService;
import it.gov.pagopa.rtp.activator.utils.Users;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import reactor.util.function.Tuples;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = { ActivationAPIControllerImpl.class })
@EnableConfigurationProperties(value = ActivationPropertiesConfig.class)
@Import({ SecurityConfig.class })
@DisabledInAotMode
class ActivationAPIControllerImplTest {

  public static final String FISCAL_CODE = "FISCAL_CODE";
  @MockitoBean
  private ActivationDBRepository activationDBRepository;

  @MockitoBean
  private ActivationPayerService activationPayerService;

  @MockitoBean
  private ActivationDtoMapper activationDtoMapper;

  private WebTestClient webTestClient;

  @Autowired
  private ApplicationContext context;

  @BeforeEach
  void setup() {
    webTestClient = WebTestClient
        .bindToApplicationContext(context)
        .apply(springSecurity())
        .configureClient()
        .build();
  }

  @Test
  @Users.RtpWriter
  void testActivatePayerSuccessful() {
    Payer payer = new Payer(ActivationID.createNew(), "RTP_SP_ID", FISCAL_CODE, Instant.now());

    when(activationPayerService.activatePayer(any(String.class), any(String.class)))
        .thenReturn(Mono.just(payer));

    webTestClient.post()
        .uri("/activations")
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .bodyValue(generateActivationRequest())
        .exchange()
        .expectStatus().isCreated().expectHeader()
        .location("http://localhost:8080/" + payer.activationID().getId().toString());
  }

  @Test
  @Users.RtpWriter
  void testActivatePayerAlreadyExists() {
    when(activationPayerService.activatePayer(any(String.class),
        any(String.class)))
        .thenReturn(Mono.error(new PayerAlreadyExists()));
    webTestClient.post()
        .uri("/activations")
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .bodyValue(generateActivationRequest())
        .exchange()
        .expectStatus().isEqualTo(409);
  }


  @Test
  @Users.RtpWriter
  void givenBadFiscalCode_whenActivatePayer_thenReturnBadRequest() {

    String invalidJson = """
            {
                "payer": {
                    "fiscalCode": "INVALID",
                    "rtpSpId": "FAKESP00"
                }
            }
            """;

    // When: Sending a POST request with invalid type
    webTestClient.post()
            .uri("/activations")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidJson)
            .exchange()
            // Then: Verify the response
            .expectStatus().isBadRequest()
            .expectBody(ErrorsDto.class);

    verify(activationPayerService, times(0)).activatePayer(any(String.class), any(String.class));
    verify(activationDtoMapper, times(0)).toActivationDto(any());
  }


  @Test
  @WithMockUser(value = "another", roles = Users.ACTIVATION_WRITE_ROLE)
  void authorizedUserShouldNotActivateForAnotherServiceProvider() {
    webTestClient.post()
        .uri("/activations")
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .bodyValue(generateActivationRequest())
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @WithMockUser
  void userWithoutEnoughPermissionShouldNotCreateNewActivation() {
    webTestClient.post()
        .uri("/activations")
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .bodyValue(generateActivationRequest())
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @Users.RtpSenderWriter
  void testFindActivationByPayerIdSuccess() {
    ActivationID activationID = ActivationID.createNew();

    Payer payer = new Payer(activationID, "testRtpSpId", "RSSMRA85T10A562S", Instant.now());

    PayerDto payerDto = new PayerDto().fiscalCode(payer.fiscalCode()).rtpSpId(payer.serviceProviderDebtor());

    ActivationDto activationDto = new ActivationDto();
    activationDto.setId(activationID.getId());
    activationDto.setPayer(payerDto);
    activationDto.setEffectiveActivationDate(null);

    when(activationPayerService.findPayer(payerDto.getFiscalCode()))
        .thenReturn(Mono.just(payer));
    when(activationDtoMapper.toActivationDto(payer))
        .thenReturn(activationDto);

    webTestClient.get()
        .uri("/activations/payer")
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .header("PayerId", payerDto.getFiscalCode())
        .exchange()
        .expectStatus().isOk()
        .expectBody(ActivationDto.class)
        .value(dto -> {
          assert dto.getPayer().getFiscalCode().equals(payer.fiscalCode());
        });
  }

  @Test
  @Users.RtpReader
  void getActivationThrowsException() {

    webTestClient.get()
        .uri("/activations/activation/{activationId}", UUID.randomUUID().toString())
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .exchange()
        .expectStatus().is4xxClientError()
        .expectBody()
        .jsonPath("$.status").isEqualTo(404)
        .jsonPath("$.error").isEqualTo("Not Found");
  }

  @Test
  @Users.RtpReader
  void testGetActivationsSuccess() {
    int page = 0;
    int size = 10;

    Payer payer = new Payer(ActivationID.createNew(), SERVICE_PROVIDER_ID, FISCAL_CODE, Instant.now());
    List<Payer> payerList = List.of(payer, payer);

    ActivationDto activationDto = new ActivationDto()
        .id(UUID.randomUUID())
        .payer(new PayerDto().fiscalCode(FISCAL_CODE).rtpSpId(SERVICE_PROVIDER_ID));

    PageMetadataDto metadata = new PageMetadataDto();
    metadata.setPage(page);
    metadata.setSize(size);
    metadata.setTotalElements(1L);
    metadata.setTotalPages(1L);

    PageOfActivationsDto expectedPage = new PageOfActivationsDto();
    expectedPage.setActivations(List.of(activationDto));
    expectedPage.setPage(metadata);

    when(activationPayerService.getActivationsByServiceProvider(SERVICE_PROVIDER_ID, page, size))
        .thenReturn(Mono.just(Tuples.of(payerList, 1L)));
    when(activationDtoMapper.toPageDto(payerList, 1L, page, size))
        .thenReturn(expectedPage);

    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/activations")
            .queryParam("page", page)
            .queryParam("size", size)
            .build())
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.activations.length()").isEqualTo(1)
        .jsonPath("$.activations[0].payer.fiscalCode").isEqualTo(FISCAL_CODE)
        .jsonPath("$.page.totalElements").isEqualTo(1)
        .jsonPath("$.page.totalPages").isEqualTo(1)
        .jsonPath("$.page.page").isEqualTo(page)
        .jsonPath("$.page.size").isEqualTo(size);
  }

  @Test
  @Users.RtpReader
  void getActivationsThrowsException() {
    when(activationPayerService.getActivationsByServiceProvider(any(), anyInt(), anyInt()))
        .thenReturn(Mono.error(new RuntimeException("Something went wrong")));

    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/activations")
            .queryParam("page", 0)
            .queryParam("size", 10)
            .build())
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .exchange()
        .expectStatus().is5xxServerError();
  }

  @ParameterizedTest
  @ValueSource(strings = { "rssmra85t10a562s", "RSSMRA85T10A56HS" })
  @Users.RtpSenderWriter
  void givenBadFiscalCodeWhenFindActivationThen400(String badFiscalCode) {

    webTestClient.get()
        .uri("/activations/payer")
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .header("PayerId", badFiscalCode)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody(ErrorsDto.class)
        .value(body -> assertThat(body.getErrors()).hasSize(1));
  }



  @Test
  @Users.RtpWriter
  void givenValidId_whenDeleteActivation_thenReturnsNoContent() {
    final var activationId = ActivationID.createNew();
    final var samplePayer = new Payer(
        activationId,
        SERVICE_PROVIDER_ID,
        FISCAL_CODE,
        Instant.now()
    );

    when(activationPayerService.findPayerById(activationId.getId()))
        .thenReturn(Mono.just(samplePayer));
    when(activationPayerService.deactivatePayer(samplePayer))
        .thenReturn(Mono.just(samplePayer));

    webTestClient
        .delete()
        .uri(uriBuilder -> uriBuilder
            .path("/activations/{activationId}")
            .build(activationId.getId().toString()))
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .exchange()
        .expectStatus().isNoContent();
  }

  @Test
  @Users.RtpWriter
  void givenMissingActivation_whenDeleteActivation_thenReturnsNotFound() {
    final var activationId = ActivationID.createNew();

    when(activationPayerService.findPayerById(activationId.getId()))
        .thenReturn(Mono.empty());

    webTestClient
        .delete()
        .uri(uriBuilder -> uriBuilder
            .path("/activations/{activationId}")
            .build(activationId.getId().toString()))
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  @Users.RtpWriter
  void givenInvalidFiscalCode_whenDeleteActivation_thenReturnsNotFound() {
    final var activationId = ActivationID.createNew();
    final var samplePayer = new Payer(
        activationId,
        "SP1",
        "INVALID",
        Instant.now()
    );

    when(activationPayerService.findPayerById(activationId.getId()))
        .thenReturn(Mono.just(samplePayer));

    webTestClient
        .delete()
        .uri(uriBuilder -> uriBuilder
            .path("/activations/{activationId}")
            .build(activationId.getId().toString()))
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .exchange()
        .expectStatus().isNotFound();
  }


  @Test
  @Users.RtpWriter
  void givenPayerWithNullServiceProviderDebtor_whenDeleteActivation_thenReturnsNotFound() {
    final var activationId = ActivationID.createNew();
    final var samplePayer = new Payer(
        activationId,
        null,
        "INVALID",
        Instant.now()
    );

    when(activationPayerService.findPayerById(activationId.getId()))
        .thenReturn(Mono.just(samplePayer));

    webTestClient
        .delete()
        .uri(uriBuilder -> uriBuilder
            .path("/activations/{activationId}")
            .build(activationId.getId().toString()))
        .header("RequestId", UUID.randomUUID().toString())
        .header("Version", "v1")
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  @Users.RtpReader
  void givenValidActivationId_whenGetActivation_thenReturn200AndBody() {
    ActivationID activationID = ActivationID.createNew();

    Payer payer = new Payer(activationID, "testRtpSpId", "RSSMRA85T10A562S", Instant.now());

    PayerDto payerDto = new PayerDto().fiscalCode(payer.fiscalCode()).rtpSpId(payer.serviceProviderDebtor());

    ActivationDto activationDto = new ActivationDto();
    activationDto.setId(activationID.getId());
    activationDto.setPayer(payerDto);
    activationDto.setEffectiveActivationDate(null);

    when(activationPayerService.findPayerById(activationDto.getId())).thenReturn(Mono.just(payer));
    when(activationDtoMapper.toActivationDto(payer)).thenReturn(activationDto);

    webTestClient.get()
            .uri("/activations/{activationId}", activationDto.getId())
            .header("RequestId", UUID.randomUUID().toString())
            .header("Version", "v1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(ActivationDto.class)
            .value(dto -> {
              assertThat(dto.getId()).isEqualTo(activationID.getId());
              assertThat(dto.getPayer().getFiscalCode()).isEqualTo("RSSMRA85T10A562S");
            });
  }

  @Test
  @Users.RtpReader
  void givenUnknownActivationId_whenGetActivation_thenReturn404() {
    UUID activationId = UUID.randomUUID();
    UUID requestId = UUID.randomUUID();

    when(activationPayerService.findPayerById(activationId))
            .thenReturn(Mono.error(new PayerNotFoundException(activationId)));

    webTestClient.get()
            .uri("/activations/{activationId}", activationId)
            .header("RequestId", requestId.toString())
            .header("Version", "v1")
            .exchange()
            .expectStatus().isNotFound();
  }

  @Test
  @Users.RtpReader
  void givenGenericError_whenGetActivation_thenReturn500() {
    UUID activationId = UUID.randomUUID();
    UUID requestId = UUID.randomUUID();

    when(activationPayerService.findPayerById(activationId))
            .thenReturn(Mono.error(new RuntimeException("Unexpected failure")));

    webTestClient.get()
            .uri("/activations/{activationId}", activationId)
            .header("RequestId", requestId.toString())
            .header("Version", "v1")
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody()
            .jsonPath("$.error").exists();
  }


  private ActivationReqDto generateActivationRequest() {
    return new ActivationReqDto(new PayerDto("RSSMRA85T10A562S", SERVICE_PROVIDER_ID));
  }
}
