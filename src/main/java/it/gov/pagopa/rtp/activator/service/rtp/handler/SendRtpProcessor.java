package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import lombok.NonNull;
import reactor.core.publisher.Mono;

/**
 * Defines the contract for processing and sending Request-to-Pay (RTP) messages
 * to the debtor's service provider.
 * <p>
 * Implementations of this interface are responsible for handling the necessary
 * transformations, validations, and external interactions required to successfully
 * send an RTP message.
 */
public interface SendRtpProcessor {

  /**
   * Sends the given {@link Rtp} request to the service provider of the debtor.
   * <p>
   * The implementation should ensure that the RTP request undergoes any necessary
   * processing.
   *
   * @param rtpToSend the RTP request to be sent
   * @return a {@link Mono} emitting the processed RTP request upon success
   *         or an error if the sending process fails
   */
  Mono<Rtp> sendRtpToServiceProviderDebtor(@NonNull final Rtp rtpToSend);
}

