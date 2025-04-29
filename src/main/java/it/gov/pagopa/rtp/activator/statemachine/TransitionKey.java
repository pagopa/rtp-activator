package it.gov.pagopa.rtp.activator.statemachine;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;


@Validated
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public abstract class TransitionKey<S, E> {
    @NotNull
    private final S source;
    @NotNull
    private final E event;
}
