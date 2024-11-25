package it.gov.pagopa.rtp.activator.controller;

import it.gov.pagopa.rtp.activator.configuration.ActivationPropertiesConfig;
import it.gov.pagopa.rtp.activator.configuration.SecurityConfig;
import it.gov.pagopa.rtp.activator.domain.Payer;
import it.gov.pagopa.rtp.activator.domain.PayerID;
import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import it.gov.pagopa.rtp.activator.model.generated.ActivationReqDto;
import it.gov.pagopa.rtp.activator.model.generated.PayerDto;
import it.gov.pagopa.rtp.activator.service.ActivationPayerService;
import it.gov.pagopa.rtp.activator.utils.Users;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static it.gov.pagopa.rtp.activator.utils.Users.SERVICE_PROVIDER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = { ActivationAPIControllerImpl.class })
@Import(SecurityConfig.class)
class ActivationAPIControllerImplTest {
    @Autowired
    ApplicationContext context;

    @Mock
    private ActivationPayerService activationPayerService;

    @Mock
    private ActivationPropertiesConfig activationPropertiesConfig;

    @InjectMocks
    private ActivationAPIControllerImpl activationAPIController;

    private ActivationReqDto activationReqDto;

    private WebTestClient web;

    @BeforeEach
    public void setup() {
        activationReqDto = new ActivationReqDto();
        web = WebTestClient
                .bindToApplicationContext(this.context)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    @Users.RtpWriter
    public void testActivatePayerSuccessful() {
        Payer payer = new Payer(PayerID.createNew(), "RTP_SP_ID", "FISCAL_CODE", Instant.now());
        when(activationPayerService.activatePayer(any(String.class), any(String.class))).thenReturn(Mono.just(payer));
        when(activationPropertiesConfig.getBaseUrl()).thenReturn("http://localhost:8080/");
        web.post()
                .uri(uriBuilder -> uriBuilder.path("/activation").queryParam("requestId", UUID.randomUUID().toString())
                        .queryParam("version", "v1").build())
                .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(activationReqDto)).exchange()
                .expectStatus().isCreated().expectHeader()
                .location("http://localhost:8080/" + payer.payerID().toString());
    }

    @Test
    @Users.RtpWriter
    public void testActivatePayerAlreadyExists() {
        when(activationPayerService.activatePayer(any(String.class), any(String.class)))
                .thenReturn(Mono.error(new PayerAlreadyExists()));
        web.post()
                .uri(uriBuilder -> uriBuilder.path("/activation").queryParam("requestId", UUID.randomUUID().toString())
                        .queryParam("version", "v1").build())
                .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(activationReqDto)).exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @Users.RtpWriter
    void shouldCreateNewActivation() {
        web.post()
                .uri("/activations")
                .header("RequestId", UUID.randomUUID().toString())
                .header("Version", "v1")
                .bodyValue(generateActivationRequest())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED)
                .expectHeader().exists(HttpHeaders.LOCATION);
    }

    @Test
    @WithMockUser(value = "another", roles = Users.ACTIVATION_WRITE_ROLE)
    void authorizedUserShouldNotActivateForAnotherServiceProvider() {
        web.post()
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
        web.post()
                .uri("/activations")
                .header("RequestId", UUID.randomUUID().toString())
                .header("Version", "v1")
                .bodyValue(generateActivationRequest())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ActivationReqDto generateActivationRequest() {
        return new ActivationReqDto(new PayerDto("RSSMRA85T10A562S", SERVICE_PROVIDER_ID));
    }
}