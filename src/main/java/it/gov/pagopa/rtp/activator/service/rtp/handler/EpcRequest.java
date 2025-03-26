package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProviderFullData;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import java.util.Objects;
import lombok.With;
import org.springframework.lang.NonNull;


/**
 * A record representing a request for an EPC (Electronic Payment Confirmation).
 *
 * @param rtpToSend the RTP (Request to Pay) to be sent
 * @param serviceProviderFullData the full data of the service provider
 * @param token an optional token for authentication or identification
 * @param response an optional response from a synchronous SEPA request to pay creation
 */
@With
public record EpcRequest(
    Rtp rtpToSend,
    ServiceProviderFullData serviceProviderFullData,
    String token,
    Object response,
    Class<?> responseClass
) {

  /**
   * Creates an instance of {@link EpcRequest} with the specified RTP to send.
   *
   * @param rtpToSend the RTP to send; must not be null
   * @return a new instance of {@link EpcRequest} with the specified RTP and null for other fields
   * @throws NullPointerException if the provided rtpToSend is null
   */
  public static <T> EpcRequest of(
      @NonNull final Rtp rtpToSend,
      @NonNull final Class<T> responseClass) {

    Objects.requireNonNull(rtpToSend, "Rtp to send cannot be null.");

    return new EpcRequest(rtpToSend, null, null, null, responseClass);
  }

}
