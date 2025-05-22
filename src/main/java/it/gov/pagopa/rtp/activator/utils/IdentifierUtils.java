package it.gov.pagopa.rtp.activator.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

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
  public static String uuidFormatter(final UUID uuid) {
    return Optional.ofNullable(uuid)
        .map(UUID::toString)
        .map(s -> s.replace("-", ""))
        .orElseThrow(() -> new IllegalArgumentException("uuid cannot be null"));
  }
}
