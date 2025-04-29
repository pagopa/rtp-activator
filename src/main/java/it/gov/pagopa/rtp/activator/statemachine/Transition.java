package it.gov.pagopa.rtp.activator.statemachine;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
@Getter
public abstract class Transition<T, S, E> {

  @NotNull
  private final S source;

  @NotNull
  private final E event;

  @NotNull
  private final S destination;

  @NotNull
  private final List<Consumer<T>> preTransactionActions;

  @NotNull
  private final List<Consumer<T>> postTransactionActions;

}
