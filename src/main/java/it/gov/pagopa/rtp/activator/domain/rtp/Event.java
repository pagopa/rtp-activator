package it.gov.pagopa.rtp.activator.domain.rtp;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Builder;
import lombok.With;
import org.springframework.validation.annotation.Validated;

@With
@Builder
@Validated
public record Event(
    @NotNull Instant timestamp,
    RtpStatus precStatus,
    @NotNull RtpEvent triggerEvent
) {

}
