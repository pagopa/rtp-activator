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


/**
 * Utility class for logging and string sanitization used primarily in RTP-related operations.
 * <p>
 * This class provides helper methods to:
 * <ul>
 *   <li>Log any object as a JSON string using a provided {@link ObjectMapper}.</li>
 *   <li>Sanitize objects by removing newline and carriage return characters from their string representation.</li>
 * </ul>
 * <p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class LoggingUtils {

  /**
   * Logs the supplied object as a JSON string using the given {@link ObjectMapper}.
   *
   * @param objectSupplier a supplier providing the object to log.
   * @param objectMapper   the {@link ObjectMapper} used for serialization.
   * @param <T>            the type of object to log.
   * @throws NullPointerException if either {@code objectSupplier} or {@code objectMapper} is {@code null}.
   */
  public static <T> void logAsJson(
      @NonNull final Supplier<T> objectSupplier,
      @NonNull final ObjectMapper objectMapper) {

    Objects.requireNonNull(objectSupplier, "Object supplier cannot be null");
    Objects.requireNonNull(objectMapper, "Object mapper cannot be null");

    try {
      final var requestToLog = objectMapper.writeValueAsString(objectSupplier.get());

      log.info(requestToLog);

    } catch (JsonProcessingException e) {
      log.error("Problem while serializing object to JSON", e);
    }
  }


  /**
   * Returns a sanitized version of the given object's string representation by removing newline (`\n`)
   * and carriage return (`\r`) characters.
   *
   * @param obj the object to sanitize, may be {@code null}.
   * @return a sanitized string without line breaks, or the literal string {@code "null"} if the object is {@code null}.
   */
  @NonNull
  public static String sanitize(@Nullable final Object obj) {
    return Optional.ofNullable(obj)
        .map(Object::toString)
        .map(string -> StringUtils.replace(string, "\n", ""))
        .map(string -> StringUtils.replace(string, "\r", ""))
        .orElse("null");
  }
}

