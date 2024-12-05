package it.gov.pagopa.rtp.activator.controller;

import static it.gov.pagopa.rtp.activator.utils.Authorizations.verifySubjectRequest;

import java.net.URI;

import it.gov.pagopa.rtp.activator.configuration.ActivationPropertiesConfig;
import it.gov.pagopa.rtp.activator.controller.generated.send.RtpsApi;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import it.gov.pagopa.rtp.activator.service.ActivationPayerService;
import it.gov.pagopa.rtp.activator.service.SendRTPService;
import it.gov.pagopa.rtp.activator.service.SepaRequestToPayMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Validated
public class SendAPIControllerImpl implements RtpsApi {

    private final SendRTPService sendRTPService;

    private SepaRequestToPayMapper sepaRequestToPayMapper;

    public SendAPIControllerImpl(SendRTPService sendRTPService) {
        this.sendRTPService = sendRTPService;
    }

    
    @Override
    @PreAuthorize("hasRole('write_rtp_send')")
    public Mono<ResponseEntity<Void>> createRtp(Mono<CreateRtpDto> createRtpDto,
        ServerWebExchange exchange
        ) {
        return verifySubjectRequest(createRtpDto, CreateRtpDto::getPayerId)
            .thenReturn(ResponseEntity.status(201).build());
    }
}
}
