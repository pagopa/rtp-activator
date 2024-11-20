package it.gov.pagopa.rtp.activator.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import it.gov.pagopa.rtp.activator.controller.generated.CreateApi;
import it.gov.pagopa.rtp.activator.model.generated.ActivationReqDto;
import reactor.core.publisher.Mono;

@RestController
@Validated
public class ActivationAPIControllerImpl implements CreateApi {

    @Override
    @PreAuthorize("hasRole('write_rtp_activations')")
    public Mono<ResponseEntity<Void>> activate(
            UUID requestId,
            String version,
            Mono<ActivationReqDto> activationReqDto,
            ServerWebExchange exchange
    ) {
        return activationReqDto.flatMap(
                request -> Mono.just(ResponseEntity.created(URI.create("http://localhost")).build())
        );
    }
}
