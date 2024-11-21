package it.gov.pagopa.rtp.activator.controller;

import it.gov.pagopa.rtp.activator.controller.generated.CreateApi;
import it.gov.pagopa.rtp.activator.model.generated.ActivationReqDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

import static it.gov.pagopa.rtp.activator.utils.Authorizations.verifySubjectRequest;

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
        return verifySubjectRequest(activationReqDto, it -> it.getPayer().getRtpSpId())
                .map(request -> ResponseEntity.created(URI.create("http://localhost")).build());
    }
}
