package it.gov.pagopa.rtp.activator.utils;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;


@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionUtils {

  @NonNull
  public static Throwable gracefullyHandleError(@NonNull final Throwable ex) {
    return Optional.of(ex)
        .map(Throwable::getCause)
        .orElse(ex);
  }

}
