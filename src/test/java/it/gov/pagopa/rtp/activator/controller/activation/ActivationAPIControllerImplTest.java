package it.gov.pagopa.rtp.activator.controller.activation;

import it.gov.pagopa.rtp.activator.configuration.ActivationPropertiesConfig;
import it.gov.pagopa.rtp.activator.configuration.SecurityConfig;
import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.ActivationID;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationReqDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PayerDto;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationDBRepository;
import it.gov.pagopa.rtp.activator.service.activation.ActivationPayerService;
import it.gov.pagopa.rtp.activator.utils.Users;
import reactor.core.publisher.Mono;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.UUID;

import static it.gov.pagopa.rtp.activator.utils.Users.SERVICE_PROVIDER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = { ActivationAPIControllerImpl.class })
@Import({ SecurityConfig.class })
@DisabledInAotMode
class ActivationAPIControllerImplTest {

    @MockBean
    private ActivationDBRepository activationDBRepository;

    @MockBean
    private ActivationPayerService activationPayerService;

    @MockBean
    private ActivationDtoMapper activationDtoMapper;

    @MockBean
    private ActivationPropertiesConfig activationPropertiesConfig;

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
        Payer payer = new Payer(ActivationID.createNew(), "RTP_SP_ID", "FISCAL_CODE", Instant.now());

        when(activationPayerService.activatePayer(any(String.class), any(String.class)))
                .thenReturn(Mono.just(payer));

        when(activationPropertiesConfig.baseUrl()).thenReturn("http://localhost:8080/");

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

        PayerDto payerDto = new PayerDto().fiscalCode(payer.fiscalCode()).rtpSpId(payer.rtpSpId());

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
    void getActivationsThrowsException() {

        webTestClient.get()
                .uri(uriBuilder -> 
                    uriBuilder
                    .path("/activations")
                    .queryParam("PageNumber", 0)
                    .queryParam("PageSize", 10)
                    .build())
                .header("RequestId", UUID.randomUUID().toString())
                .header("Version", "v1")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request");
    }

    private ActivationReqDto generateActivationRequest() {
        return new ActivationReqDto(new PayerDto("RSSMRA85T10A562S", SERVICE_PROVIDER_ID));
    }
}
