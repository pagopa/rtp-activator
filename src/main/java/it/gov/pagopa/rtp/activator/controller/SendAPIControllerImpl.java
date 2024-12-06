package it.gov.pagopa.rtp.activator.controller;

import it.gov.pagopa.rtp.activator.controller.generated.send.RtpsApi;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import it.gov.pagopa.rtp.activator.service.rtp.SendRTPService;

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

    public SendAPIControllerImpl(SendRTPService sendRTPService) {
        this.sendRTPService = sendRTPService;
    }

    @Override
    @PreAuthorize("hasRole('write_rtp_send')")
    public Mono<ResponseEntity<Void>> createRtp(Mono<CreateRtpDto> createRtpDto,
            ServerWebExchange exchange) {
        return createRtpDto
                .flatMap(t -> sendRTPService.send(t.getNoticeNumber(), t.getAmount(), t.getDescription(),
                        t.getExpiryDate(), t.getPayerId(), t.getPayee().getName(), t.getPayee().getPayeeId(), "endToEndId",
                        "iban", "payTrxRef", "flgConf"))
                .thenReturn(ResponseEntity.status(201).build());
    }
}
