package it.gov.pagopa.rtp.activator.controller;

import it.gov.pagopa.rtp.activator.controller.generated.send.RtpsApi;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpMapper;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import it.gov.pagopa.rtp.activator.service.rtp.SendRTPService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Validated
public class SendAPIControllerImpl implements RtpsApi {

    private final SendRTPService sendRTPService;

    private final RtpMapper rtpMapper;

    public SendAPIControllerImpl(SendRTPService sendRTPService, RtpMapper rtpMapper) {
        this.sendRTPService = sendRTPService;
        this.rtpMapper = rtpMapper;
    }

    @Override
    public Mono<ResponseEntity<Void>> createRtp(Mono<CreateRtpDto> createRtpDto,
            ServerWebExchange exchange) {
        return createRtpDto
                .map(rtpDto -> rtpMapper.toRtp(rtpDto))
                .flatMap(rtp -> sendRTPService.send(rtp))
                .thenReturn(ResponseEntity.status(201).build());
    }
}
