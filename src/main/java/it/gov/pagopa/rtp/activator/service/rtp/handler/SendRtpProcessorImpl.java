package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.utils.ExceptionUtils;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component("sendRtpProcessorImpl")
@Slf4j
public class SendRtpProcessorImpl implements SendRtpProcessor {

  private final RegistryDataHandler registryDataHandler;
  private final Oauth2Handler oauth2Handler;
  private final SendRtpHandler sendRtpHandler;


  public SendRtpProcessorImpl(
      @NonNull final RegistryDataHandler registryDataHandler,
      @NonNull final Oauth2Handler oauth2Handler,
      @NonNull final SendRtpHandler sendRtpHandler) {

    this.registryDataHandler = Objects.requireNonNull(registryDataHandler);
    this.oauth2Handler = Objects.requireNonNull(oauth2Handler);
    this.sendRtpHandler = Objects.requireNonNull(sendRtpHandler);
  }


  @NonNull
  @Override
  public Mono<Rtp> sendRtpToServiceProviderDebtor(@NonNull final Rtp rtpToSend) {

    return Mono.just(rtpToSend)
        .map(EpcRequest::of)
        .flatMap(this.registryDataHandler::handle)
        .flatMap(this.oauth2Handler::handle)
        .flatMap(this.sendRtpHandler::handle)
        .onErrorMap(ExceptionUtils::gracefullyHandleError)
        .map(response -> rtpToSend)
        .defaultIfEmpty(rtpToSend)
        .doOnSuccess(rtpSent -> log.info("RTP sent to {} with id: {}",
            rtpSent.serviceProviderDebtor(), rtpSent.resourceID().getId()))
        .doOnError(error -> log.error("Error sending RTP to {}: {}",
            rtpToSend.serviceProviderDebtor(), error.getMessage()));
  }
}
