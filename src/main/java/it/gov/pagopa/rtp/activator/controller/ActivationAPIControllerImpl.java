package it.gov.pagopa.rtp.activator.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import it.gov.pagopa.rtp.activator.controller.generated.CreateApi;
import it.gov.pagopa.rtp.activator.model.generated.ActivationReqDto;
import it.gov.pagopa.rtp.activator.service.ActivationPayerService;
import reactor.core.publisher.Mono;

@RestController
@Validated
public class ActivationAPIControllerImpl implements CreateApi {

    private final ActivationPayerService activationPayerService;

    public ActivationAPIControllerImpl(ActivationPayerService activationPayerService){
        this.activationPayerService = activationPayerService;
    }

    @Override
    public Mono<ResponseEntity<Void>> activate(UUID requestId, String version, Mono<ActivationReqDto> activationReqDto,
            ServerWebExchange exchange) {
        // TODO Auto-generated method stub
        activationPayerService.activatePayer(activationReqDto.block().getPayer().getFiscalCode(),activationReqDto.block().getPayer().getRtpSpId().toString());
        throw new UnsupportedOperationException("Unimplemented method 'activate'");
        
    }

}
