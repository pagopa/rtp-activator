package it.gov.pagopa.rtp.activator.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
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
    public Mono<ResponseEntity<Void>> activate(UUID requestId, String version, Mono<ActivationReqDto> activationReqDto,
            ServerWebExchange exchange) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'activate'");
    }

}
