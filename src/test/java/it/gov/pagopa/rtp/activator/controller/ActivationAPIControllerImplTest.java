package it.gov.pagopa.rtp.activator.controller;

import it.gov.pagopa.rtp.activator.configuration.SecurityConfig;
import it.gov.pagopa.rtp.activator.model.generated.ActivationReqDto;
import it.gov.pagopa.rtp.activator.model.generated.PayerDto;
import it.gov.pagopa.rtp.activator.utils.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;


@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = {ActivationAPIControllerImpl.class})
@Import(SecurityConfig.class)
class ActivationAPIControllerImplTest {
    @Autowired
    ApplicationContext context;

    WebTestClient web;

    @BeforeEach
    public void setup() {
        web = WebTestClient
                .bindToApplicationContext(this.context)
                .apply(springSecurity())
                .configureClient()
                .build();
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
        return new ActivationReqDto(new PayerDto("RSSMRA85T10A562S", "134"));
    }
}