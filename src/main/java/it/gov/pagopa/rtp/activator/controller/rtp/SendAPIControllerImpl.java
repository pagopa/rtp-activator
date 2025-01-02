package it.gov.pagopa.rtp.activator.controller.rtp;

import it.gov.pagopa.rtp.activator.controller.generated.send.RtpsApi;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import it.gov.pagopa.rtp.activator.service.rtp.SendRTPService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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
    @PreAuthorize("hasRole('write_rtp_send')")
    public Mono<ResponseEntity<Void>> createRtp(Mono<CreateRtpDto> createRtpDto,
            ServerWebExchange exchange) {
        return createRtpDto
            .map(rtpMapper::toRtp)
            .flatMap(sendRTPService::send)
            .thenReturn(new ResponseEntity<Void>(HttpStatus.CREATED))
            .onErrorResume(e -> {
                if (e instanceof WebClientResponseException webClientResponseException) {
                    return Mono.just(
                        ResponseEntity.status(webClientResponseException.getStatusCode()).build());
                }
                if (e instanceof WebExchangeBindException) {
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                }
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }
}
