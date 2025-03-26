package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.epcClient.model.SynchronousSepaRequestToPayCreationResponseDto;
import it.gov.pagopa.rtp.activator.utils.ExceptionUtils;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * Implementation of the {@link SendRtpProcessor} interface responsible for processing and sending RTP (Request to Pay) messages.
 * <p>
 * This class orchestrates the necessary steps for sending an RTP request by interacting with various handlers:
 * <ul>
 *   <li>RegistryDataHandler: Retrieves registry data for the service provider.</li>
 *   <li>Oauth2Handler: Handles OAuth2 authentication if required.</li>
 *   <li>SendRtpHandler: Sends the actual RTP request.</li>
 * </ul>
 */
@Component("sendRtpProcessorImpl")
@Slf4j
public class SendRtpProcessorImpl implements SendRtpProcessor {

  private final RegistryDataHandler registryDataHandler;
  private final Oauth2Handler oauth2Handler;
  private final SendRtpHandler sendRtpHandler;

  /**
   * Constructs a {@code SendRtpProcessorImpl} with the necessary handlers.
   *
   * @param registryDataHandler The handler responsible for fetching registry data.
   * @param oauth2Handler The handler responsible for OAuth2 authentication.
   * @param sendRtpHandler The handler responsible for sending the RTP request.
   * @throws NullPointerException if any of the provided handlers are {@code null}.
   */
  public SendRtpProcessorImpl(
      @NonNull final RegistryDataHandler registryDataHandler,
      @NonNull final Oauth2Handler oauth2Handler,
      @NonNull final SendRtpHandler sendRtpHandler) {

    this.registryDataHandler = Objects.requireNonNull(registryDataHandler);
    this.oauth2Handler = Objects.requireNonNull(oauth2Handler);
    this.sendRtpHandler = Objects.requireNonNull(sendRtpHandler);
  }

  /**
   * Processes and sends an RTP request to the service provider debtor.
   *
   * <p>The processing follows these steps:
   * <ol>
   *   <li>Wraps the RTP request in an {@link EpcRequest}.</li>
   *   <li>Fetches registry data.</li>
   *   <li>Handles OAuth2 authentication if required.</li>
   *   <li>Sends the RTP request.</li>
   *   <li>Handles errors gracefully and logs success or failure.</li>
   * </ol>
   *
   * @param rtpToSend The RTP request to be sent.
   * @return A {@link Mono} emitting the sent RTP request or an error if the process fails.
   */
  @NonNull
  @Override
  public Mono<Rtp> sendRtpToServiceProviderDebtor(@NonNull final Rtp rtpToSend) {
    return Mono.just(rtpToSend)
        .doFirst(() -> log.info("Sending RTP to {}", rtpToSend.serviceProviderDebtor()))
        .doOnNext(rtp -> log.debug("Creating EPC request."))
        .map(rtp -> EpcRequest.of(rtp, SynchronousSepaRequestToPayCreationResponseDto.class))
        .flatMap(this::handleIntermediateSteps)
        .doOnNext(epcRequest -> log.debug("Calling send RTP handler."))
        .flatMap(this.sendRtpHandler::handle)
        .onErrorMap(ExceptionUtils::gracefullyHandleError)
        .map(response -> rtpToSend)
        .defaultIfEmpty(rtpToSend)
        .doOnSuccess(rtpSent -> log.info("RTP sent to {} with id: {}",
            rtpSent.serviceProviderDebtor(), rtpSent.resourceID().getId()))
        .doOnError(error -> log.error("Error sending RTP to {}: {}",
            rtpToSend.serviceProviderDebtor(), error.getMessage()));
  }

  /**
   * Handles intermediate processing steps for the EPC request.
   *
   * <p>This method performs the following actions:
   * <ul>
   *   <li>Logs EPC request creation.</li>
   *   <li>Calls the registry data handler.</li>
   *   <li>Calls the OAuth2 handler for authentication.</li>
   * </ul>
   *
   * @param epcRequest The EPC request to be processed.
   * @return A {@link Mono} emitting the processed EPC request.
   * @throws NullPointerException if {@code epcRequest} is {@code null}.
   */
  @NonNull
  private Mono<EpcRequest> handleIntermediateSteps(@NonNull final EpcRequest epcRequest) {
    Objects.requireNonNull(epcRequest);

    return Mono.just(epcRequest)
        .doOnNext(request -> log.debug("EPC request created: {}", request))
        .doOnNext(request -> log.debug("Calling registry data handler."))
        .flatMap(this.registryDataHandler::handle)
        .doOnNext(data -> log.debug("Successfully called registry data handler."))
        .doOnNext(request -> log.debug("Calling OAuth2 handler."))
        .flatMap(this.oauth2Handler::handle)
        .doOnNext(data -> log.debug("Successfully called OAuth2 handler."));
  }
}

