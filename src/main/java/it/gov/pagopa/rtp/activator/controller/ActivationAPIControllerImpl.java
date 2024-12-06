package it.gov.pagopa.rtp.activator.controller;

import it.gov.pagopa.rtp.activator.controller.generated.activate.CreateApi;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationReqDto;
import it.gov.pagopa.rtp.activator.service.activation.ActivationPayerService;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import it.gov.pagopa.rtp.activator.configuration.ActivationPropertiesConfig;
import it.gov.pagopa.rtp.activator.domain.errors.PayerAlreadyExists;
import reactor.core.publisher.Mono;

import org.springframework.security.access.prepost.PreAuthorize;

import java.net.URI;

import static it.gov.pagopa.rtp.activator.utils.Authorizations.verifySubjectRequest;

@RestController
@Validated
public class ActivationAPIControllerImpl implements CreateApi {

    private final ActivationPayerService activationPayerService;

    private final ActivationPropertiesConfig activationPropertiesConfig;

    public ActivationAPIControllerImpl(ActivationPayerService activationPayerService,
            ActivationPropertiesConfig activationPropertiesConfig) {
        this.activationPayerService = activationPayerService;
        this.activationPropertiesConfig = activationPropertiesConfig;
    }

    @Override
    @PreAuthorize("hasRole('write_rtp_activations')")
    public Mono<ResponseEntity<Void>> activate(
            UUID requestId,
            String version,
            Mono<ActivationReqDto> activationReqDto,
            ServerWebExchange exchange) {

        return verifySubjectRequest(activationReqDto, it -> it.getPayer().getRtpSpId())
                .flatMap(t -> activationPayerService.activatePayer(t.getPayer().getRtpSpId(),
                        t.getPayer().getFiscalCode()))
                .<ResponseEntity<Void>>map(payer -> ResponseEntity
                        .created(URI.create(activationPropertiesConfig.baseUrl() + payer.payerID().toString()))
                        .build())
                .onErrorReturn(PayerAlreadyExists.class, ResponseEntity.status(409).build());
    }
}
