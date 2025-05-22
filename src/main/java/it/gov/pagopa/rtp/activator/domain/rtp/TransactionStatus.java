package it.gov.pagopa.rtp.activator.domain.rtp;

import lombok.NonNull;
import java.util.Arrays;

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

  public static TransactionStatus fromString(@NonNull final String text) {
    return Arrays.stream(TransactionStatus.values())
            .filter(b -> b.value.equals(text))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No matching Enum"));
  }
}
