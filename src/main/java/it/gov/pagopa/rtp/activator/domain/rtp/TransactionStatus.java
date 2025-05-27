package it.gov.pagopa.rtp.activator.domain.rtp;

import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enumeration of possible transaction statuses for Request-To-Pay (RTP) flows.
 */
public enum TransactionStatus {

  ACTC("ACTC"),
  ACCP("ACCP"),
  RJCT("RJCT"),
  ERROR("ERROR"),
  CNCL("CNCL"),
  RJCR("RJCR"),
  ACWC("ACWC");

  private final String value;

  TransactionStatus(String value) {
    this.value = value;
  }

  /**
   * Maps a string to the corresponding {@link TransactionStatus} enum value.
   *
   * <p>This method is case-sensitive and expects an exact match with the internal value.
   *
   * @param text the string to convert (must not be null)
   * @return the matching {@link TransactionStatus}
   * @throws IllegalArgumentException if the input is null or no match is found
   */
  @NonNull
  public static TransactionStatus fromString(final String text) {
    return Arrays.stream(TransactionStatus.values())
            .filter(b -> b.value.equals(Optional.ofNullable(text)
                            .orElseThrow(() -> new IllegalArgumentException("Input text must not be null"))
            ))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No matching Enum"));
  }
}
