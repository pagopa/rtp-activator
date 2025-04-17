package it.gov.pagopa.rtp.activator.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingUtilsTest {

  @Test
  void givenNullObject_whenSanitize_thenReturnStringNull() {
    assertThat(LoggingUtils.sanitize(null))
        .isEqualTo("null");
  }

  @Test
  void givenObjectWithoutNewLines_whenSanitize_thenReturnSameString() {
    assertThat(LoggingUtils.sanitize("SimpleText"))
        .isEqualTo("SimpleText");
  }

  @Test
  void givenObjectWithNewLine_whenSanitize_thenRemoveNewLine() {
    assertThat(LoggingUtils.sanitize("Line1\nLine2"))
        .isEqualTo("Line1Line2");
  }

  @Test
  void givenObjectWithCarriageReturn_whenSanitize_thenRemoveCarriageReturn() {
    assertThat(LoggingUtils.sanitize("Line1\rLine2"))
        .isEqualTo("Line1Line2");
  }

  @Test
  void givenObjectWithMixedNewLines_whenSanitize_thenRemoveAll() {
    assertThat(LoggingUtils.sanitize("Line1\r\nLine2\nLine3\r"))
        .isEqualTo("Line1Line2Line3");
  }

  @Test
  void givenObjectWithToString_whenSanitize_thenUseItsStringValue() {
    final var obj = new Object() {

      @Override
      public String toString() {
        return "a\nb\rc";
      }
    };

    assertThat(LoggingUtils.sanitize(obj)).isEqualTo("abc");
  }
}
