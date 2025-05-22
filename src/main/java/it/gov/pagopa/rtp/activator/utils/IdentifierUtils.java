package it.gov.pagopa.rtp.activator.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for operations related to identifiers.
 *
 * <p>This class provides helper methods for handling and formatting identifier values such as
 * UUIDs.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdentifierUtils {

  private static final String UUID_PATTERN = "^([a-f0-9]{8})-([a-f0-9]{4})-([a-f0-9]{4})-([a-f0-9]{4})-([a-f0-9]{12})$";
  private static final String DASHES_REMOVER_PATTERN = "$1$2$3$4$5";

  private static final String UUID_WITHOUT_DASHES_PATTERN = "^([a-f0-9]{8})([a-f0-9]{4})([a-f0-9]{4})([a-f0-9]{4})([a-f0-9]{12})$";
  private static final String DASHES_INSERTER_PATTERN = "$1-$2-$3-$4-$5";

  /**
   * Checks if the given string is a valid UUID.
   *
   * @param uuidString the string to be checked
   * @return true if the string is a valid UUID, false otherwise
   */
  public static boolean isValidUuid(final String uuidString) {
    return StringUtils.isNotBlank(uuidString) && uuidString.matches(UUID_WITHOUT_DASHES_PATTERN);
  }

  /**
   * Formats a {@link UUID} by removing all hyphens ("-").
   *
   * <p>This method takes a UUID, converts it to its string representation, and removes all hyphen
   * characters to produce a compact string version.
   *
   * @param uuid the UUID to be formatted (must not be null)
   * @return a hyphen-free string representation of the UUID
   * @throws IllegalArgumentException if the uuid is null
   */
  @NonNull
  public static String uuidFormatter(@NonNull final UUID uuid) {
    return Optional.of(uuid)
        .map(UUID::toString)
        .map(s -> s.replaceFirst(UUID_PATTERN, DASHES_REMOVER_PATTERN))
        .orElseThrow(() -> new IllegalArgumentException("uuid cannot be null"));
  }

  @NonNull
  public static UUID uuidRebuilder(@NonNull final String uuidString) {
    return Optional.of(uuidString)
            .map(s -> s.replaceFirst(UUID_WITHOUT_DASHES_PATTERN, DASHES_INSERTER_PATTERN))
            .map(UUID::fromString)
            .orElseThrow(() -> new IllegalArgumentException("Invalid UUID format"));
  }
}
