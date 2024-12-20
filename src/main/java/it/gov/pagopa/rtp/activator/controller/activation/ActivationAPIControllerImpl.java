package it.gov.pagopa.rtp.activator.controller.activation;

import it.gov.pagopa.rtp.activator.controller.generated.activate.CreateApi;
import it.gov.pagopa.rtp.activator.controller.generated.activate.ReadApi;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationReqDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PageOfActivationsDto;
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
public class ActivationAPIControllerImpl implements CreateApi, ReadApi {

    private final ActivationPayerService activationPayerService;

    private final ActivationPropertiesConfig activationPropertiesConfig;

    private final ActivationDtoMapper activationDtoMapper;

    public ActivationAPIControllerImpl(ActivationPayerService activationPayerService,
            ActivationPropertiesConfig activationPropertiesConfig,
            ActivationDtoMapper activationDtoMapper) {
        this.activationPayerService = activationPayerService;
        this.activationPropertiesConfig = activationPropertiesConfig;
        this.activationDtoMapper = activationDtoMapper;
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
                        .created(URI.create(activationPropertiesConfig.baseUrl()
                                + payer.activationID().getId().toString()))
                        .build())
                .onErrorReturn(PayerAlreadyExists.class, ResponseEntity.status(409).build());
    }

    @Override
    @PreAuthorize("hasRole('write_rtp_send')")
    public Mono<ResponseEntity<ActivationDto>> findActivationByPayerId(
            UUID requestId,
            String payerId,
            String version,
            ServerWebExchange exchange) {
        return Mono.just(payerId)
                .flatMap(activationPayerService::findPayer)
                .map(activationDtoMapper::toActivationDto)
                .<ResponseEntity<ActivationDto>>map(activationDto -> ResponseEntity.ok(activationDto));
    }

    @Override
    @PreAuthorize("hasRole('read_rtp_activations')")
    public Mono<ResponseEntity<ActivationDto>> getActivation(UUID requestId, UUID activationId,
            String version,
            ServerWebExchange exchange) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActivation'");
    }

    @Override
    @PreAuthorize("hasRole('read_rtp_activations')")
    public Mono<ResponseEntity<PageOfActivationsDto>> getActivations(UUID requestId,
            Integer page,
            Integer size,
            String version,
            ServerWebExchange exchange) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActivations'");
    }

}
