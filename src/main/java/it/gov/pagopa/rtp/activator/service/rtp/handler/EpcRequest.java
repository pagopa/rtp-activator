package it.gov.pagopa.rtp.activator.service.rtp.handler;

import it.gov.pagopa.rtp.activator.domain.registryfile.ServiceProviderFullData;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.epcClient.model.SynchronousSepaRequestToPayCreationResponseDto;
import java.util.Objects;
import lombok.With;
import org.springframework.lang.NonNull;


@With
public record EpcRequest(
    Rtp rtpToSend,
    ServiceProviderFullData serviceProviderFullData,
    String token,
    SynchronousSepaRequestToPayCreationResponseDto response
) {

  public static EpcRequest of(@NonNull final Rtp rtpToSend) {
    Objects.requireNonNull(rtpToSend, "Rtp to send cannot be null.");
    return new EpcRequest(rtpToSend, null, null, null);
  }

}
