package it.gov.pagopa.rtp.activator.domain.rtp;

import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Optional;

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
