package it.gov.pagopa.rtp.activator.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class LoggingUtils {

  public static <T> void logAsJson(
      @NonNull final Supplier<T> objectSupplier,
      @NonNull final ObjectMapper objectMapper) {

    Objects.requireNonNull(objectSupplier, "Object supplier cannot be null");
    Objects.requireNonNull(objectMapper, "Object mapper cannot be null");

    try {
      final var requestToLog = objectMapper.writeValueAsString(objectSupplier.get());

      log.info(requestToLog);

    } catch (JsonProcessingException e) {
      log.error("Problem while serializing SepaRequestToPayRequestResourceDto object", e);
    }
  }


  @NonNull
  public static String sanitize(@Nullable final Object obj) {
    return Optional.ofNullable(obj)
        .map(Object::toString)
        .map(string -> StringUtils.replace(string, "\n", ""))
        .map(string -> StringUtils.replace(string, "\r", ""))
        .orElse("null");
  }

}
