package it.gov.pagopa.rtp.activator.controller.rtp;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.controller.generated.send.RtpsApi;
import it.gov.pagopa.rtp.activator.domain.errors.PayerNotActivatedException;
import it.gov.pagopa.rtp.activator.domain.errors.ServiceProviderNotFoundException;
import it.gov.pagopa.rtp.activator.model.generated.send.CreateRtpDto;
import it.gov.pagopa.rtp.activator.service.rtp.SendRTPService;
import it.gov.pagopa.rtp.activator.utils.TokenInfo;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
  private final ServiceProviderConfig serviceProviderConfig;

  public SendAPIControllerImpl(SendRTPService sendRTPService, RtpDtoMapper rtpDtoMapper,
      ServiceProviderConfig serviceProviderConfig) {
    this.sendRTPService = sendRTPService;
    this.rtpDtoMapper = rtpDtoMapper;
    this.serviceProviderConfig = serviceProviderConfig;
  }

  @WithSpan
  @Override
  @PreAuthorize("hasRole('write_rtp_send')")
  public Mono<ResponseEntity<Void>> createRtp(Mono<CreateRtpDto> createRtpDto,
      String version, ServerWebExchange exchange) {
    log.info("Received request to create RTP");
    return createRtpDto
        .flatMap(rtpDto -> TokenInfo.getTokenSubject()
            .map(sub -> rtpDtoMapper.toRtpWithServiceProviderCreditor(rtpDto, sub)))
        .flatMap(sendRTPService::send)
        .doOnSuccess(rtpSaved -> MDC.put("service_provider_debtor", rtpSaved.serviceProviderDebtor()))
        .doOnSuccess(rtpSaved -> MDC.put("service_provider_creditor", rtpSaved.serviceProviderCreditor()))
        .doOnSuccess(rtpSaved -> MDC.put("payee_name", rtpSaved.payeeName()))
        .doOnSuccess(rtpSaved -> log.info("RTP sent with id: {}", rtpSaved.resourceID().getId()))
        .<ResponseEntity<Void>>map(rtp -> ResponseEntity
            .created(URI.create(serviceProviderConfig.baseUrl() + rtp.resourceID().getId()))
            .build())
        .onErrorReturn(PayerNotActivatedException.class,
            ResponseEntity.unprocessableEntity().build())
        .onErrorReturn(ServiceProviderNotFoundException.class,
            ResponseEntity.unprocessableEntity().build())
        .doOnError(a -> log.error("Error creating RTP {}", a.getMessage()))
        .doFinally(f -> MDC.clear());
  }

}
