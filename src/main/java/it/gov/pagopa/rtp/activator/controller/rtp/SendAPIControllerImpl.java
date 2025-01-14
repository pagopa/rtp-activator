package it.gov.pagopa.rtp.activator.controller.rtp;

import it.gov.pagopa.rtp.activator.controller.generated.send.RtpsApi;
import it.gov.pagopa.rtp.activator.domain.errors.PayerNotActivatedException;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import it.gov.pagopa.rtp.activator.service.rtp.SendRTPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Validated
@Slf4j
public class SendAPIControllerImpl implements RtpsApi {

    private final SendRTPService sendRTPService;

    private final RtpDtoMapper rtpDtoMapper;

    public SendAPIControllerImpl(SendRTPService sendRTPService, RtpDtoMapper rtpDtoMapper) {
        this.sendRTPService = sendRTPService;
        this.rtpDtoMapper = rtpDtoMapper;
    }

    @Override
    @PreAuthorize("hasRole('write_rtp_send')")
    public Mono<ResponseEntity<Void>> createRtp(Mono<CreateRtpDto> createRtpDto,
            ServerWebExchange exchange) {
        log.info("Received request to create RTP");
        return createRtpDto
            .map(rtpDtoMapper::toRtp)
            .flatMap(sendRTPService::send)
            .thenReturn(new ResponseEntity<Void>(HttpStatus.CREATED))
            .onErrorReturn(PayerNotActivatedException.class, ResponseEntity.unprocessableEntity().build());
    }
}
